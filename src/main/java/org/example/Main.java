package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        final String URL = "http://siops.datasus.gov.br/filtro_rel_ges_dt_municipal.php";

        WebDriver driver = new ChromeDriver();
        driver.get(URL);
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        WebElement webElementAno = driver.findElement(By.id("cmbAno"));
        Select selectAno = new Select(webElementAno);
        List<WebElement> listaAnos = selectAno.getOptions();

        for(int ano = listaAnos.size() - 1; ano >= 2; ano--) {

            webElementAno = driver.findElement(By.id("cmbAno"));
            selectAno = new Select(webElementAno);
            selectAno.selectByIndex(ano);

            WebElement elAno = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbAno")));
            selectAno = new Select(elAno);
            String stringAno = selectAno.getOptions().get(ano).getText();
            System.out.println(stringAno);

            WebElement webElementPeriodo = driver.findElement(By.id("cmbPeriodo"));
            Select selectPeriodo = new Select(webElementPeriodo);
            List<WebElement> listaPeriodos = selectPeriodo.getOptions();

            XSSFWorkbook workbook = new XSSFWorkbook();

            for (int periodo = 0; periodo < 2; periodo++) {
                webElementPeriodo = driver.findElement(By.id("cmbPeriodo"));
                selectPeriodo = new Select(webElementPeriodo);
                selectPeriodo.selectByIndex(periodo);

                WebElement elPeriodo = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbPeriodo")));
                selectPeriodo = new Select(elPeriodo);
                String stringPeriodo = selectPeriodo.getOptions().get(periodo).getText();

                WebElement webElementUf = driver.findElement(By.id("cmbUF"));
                Select selectUf = new Select(webElementUf);
                List<WebElement> listaUfs = selectUf.getOptions();
                System.out.println(stringPeriodo);

                XSSFSheet sheet = workbook.createSheet(stringPeriodo);
                Row dadosMunicipios;
                int numeroLinha = 1, idColuna = 0;
                Map<Integer, Object[]> dadosTabela = new TreeMap<>();
                dadosTabela.put(numeroLinha++, new Object[]{"UF", "IBGE", "Município", "União corrente",
                        "União capital", "Total atenção básica corrente", "Total atenção básica capital"});

                for(int uf = 0; uf < 2; uf++) {

                    webElementUf = driver.findElement(By.id("cmbUF"));
                    selectUf = new Select(webElementUf);
                    selectUf.selectByIndex(uf);

                    WebElement elUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
                    selectUf = new Select(elUf);
                    String stringUf = selectUf.getOptions().get(uf).getText();
                    //System.out.println("UF: " + stringUf);

                    WebElement webElementMunicipio = driver.findElement(By.id("cmbMunicipio"));
                    Select selectMunicipio = new Select(webElementMunicipio);
                    List<WebElement> listaMunicipios = selectMunicipio.getOptions();

                    for(int municipio = 0; municipio < 2; municipio++) {

                        webElementMunicipio = driver.findElement(By.id("cmbMunicipio"));
                        selectMunicipio = new Select(webElementMunicipio);
                        selectMunicipio.selectByIndex(municipio);

                        WebElement elMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio")));
                        selectMunicipio = new Select(elMunicipio);
                        String valorIBGE = elMunicipio.getAttribute("value");
                        String nomeMunicipio = selectMunicipio.getOptions().get(municipio).getText();

//                        String stringMunicipio = selectMunicipio.getOptions().get(municipio).getAttribute("value");
//                        System.out.println(stringMunicipio);

                        WebElement submitButton = driver.findElement(By.name("BtConsultar"));
                        submitButton.click();

                        try {
                            //valoresTabelaPorMunicípio(driver);
                            String uniaoCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(4).getText();
                            String totalCorrente; // = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(10).getText();
                            String uniaoCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(3).getText();
                            String totalCapital; // = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(9).getText();
                            // String total = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(16).findElements(By.tagName("td")).get(3).getText();

                            if(stringAno.equals("2020")) { // resultados errados na planilha quando em 2020
                                totalCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(10).getText();
                                totalCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(9).getText();
                            } else {
                                totalCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(11).getText();
                                totalCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(10).getText();
                            }

                            dadosTabela.put(numeroLinha++, new Object[]{stringUf, valorIBGE, nomeMunicipio, uniaoCorrente, uniaoCapital, totalCorrente, totalCapital});

                        } catch (Exception e) {
                            System.out.println(e);
                        }

                        // voltar
                        driver.navigate().back();
                    }
                }

                for(Integer key : dadosTabela.keySet()) {
                    dadosMunicipios = sheet.createRow(idColuna++);
                    Object[] objectArr = dadosTabela.get(key);
                    int cellId = 0;

                    for(Object obj : objectArr) {
                        Cell cell = dadosMunicipios.createCell(cellId++);
                        cell.setCellValue((String)obj);
                    }
                }
            }

            FileOutputStream out = new FileOutputStream("C:/Users/Naionara Ramos/Projetos/robot/planilhas/" + stringAno + ".xlsx");
            workbook.write(out);
            out.close();
        }

        driver.close();
        System.exit(0);
    }

    private static void valoresTabelaPorMunicípio(WebDriver driver) {
        String uniaoCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(4).getText();
        String totalCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(10).getText();
        String uniaoCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(3).getText();
        String totalCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(9).getText();
        String total = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(16).findElements(By.tagName("td")).get(3).getText();
        System.out.println("uniaoCorrente: " + uniaoCorrente);
        System.out.println("uniaoCapital: " + uniaoCapital);
        System.out.println("totalCorrente: " + totalCorrente);
        System.out.println("totalCapital: " + totalCapital);
        System.out.println("total: " + total);
       // return Arrays.asList(uniaoCorrente, uniaoCapital, totalCorrente, totalCapital, total);
    }

    // essa é de verdade
    private static void criacaoPlanilhas() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Teste");
        //XSSFSheet sheet1 = workbook.createSheet("Segunda Folha");
        Row dadosMunicipios = sheet.createRow(0);

        Map<Integer, Object[]> dadosTabela = new TreeMap<>();
        dadosTabela.put(1, new Object[]{"UF", "IBGE", "Município", "União corrente",
                "União capital", "Total atenção básica corrente", "Total atenção básica capital"});

        int idCol = 0;

        for(Integer key : dadosTabela.keySet()) {
            dadosMunicipios = sheet.createRow(idCol++);
            Object[] objectArr = dadosTabela.get(key);
            int cellId = 0;

            for(Object obj : objectArr) {
                Cell cell = dadosMunicipios.createCell(cellId++);
                cell.setCellValue((String)obj);
            }
        }

//        CellStyle cellStyle = CellStyle.BORDER_THICK;
//        cellStyle.setFillBackgroundColor(new Color());
//        sheet.addMergedRegion(CellRangeAddress.valueOf("A4:D4"));

        FileOutputStream out = new FileOutputStream("C:/Users/Naionara Ramos/Projetos/robot/planilhas/folhas.xlsx");
        workbook.write(out);
        out.close();
    }
    private void montandoPlanilha(String titulo, Map<String, List<String>> mapeamentoMunicípioValores) throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(titulo);
        Row nomesMunicipios = sheet.createRow(0);

        Map<String, Object[]> studentData = new TreeMap<>();
        studentData.put("1", new Object[]{"128", "2 ano"});
        studentData.put("2", new Object[]{"149", "3 ano"});

        int idCol = 0;

        for(String key : studentData.keySet()) {
            nomesMunicipios = sheet.createRow(idCol++);
            Object[] objectArr = studentData.get(key);
            int cellId = 0;

            for(Object obj : objectArr) {
                Cell cell = nomesMunicipios.createCell(cellId++);
                cell.setCellValue((String)obj);
            }
        }

//        Cell municipio = nomesMunicipios.createCell(0);
//        municipio.setCellValue("Município");
//
//        Cell uniaoCorrente = nomesMunicipios.createCell(1);
//        municipio.setCellValue("União - Corrente");
//
//        Cell uniaoCapital = nomesMunicipios.createCell(2);
//        municipio.setCellValue("União - Capital");
//
//        Cell totalCorrente = nomesMunicipios.createCell(3);
//        municipio.setCellValue("Total Corrente");
//
//        Cell totalCapital = nomesMunicipios.createCell(4);
//        municipio.setCellValue("Total Capital");
//
//        Cell totalUniao = nomesMunicipios.createCell(5);
//        municipio.setCellValue("Total - União");

//        int contador = 1;
//
//        Row colunaNome;
//        Cell cellCity;
//
//        for(int i = 0; i < mapeamentoMunicípioValores.size(); i++) {
//            sheet.createRow(1);
//            //rowCity.createCell(0);
//            //cellCity.setCellValue("Município");
//        }

        FileOutputStream out = new FileOutputStream("C:/Users/Naionara Ramos/Projetos/robot/planilhas/planilha.xlsx");
        workbook.write(out);
        out.close();
    }

    private static void deuErradoReverDepoisOndeTaATreta(WebDriver driver) {
        WebElement ano = driver.findElement(By.id("cmbAno"));
        WebElement periodo = driver.findElement(By.name("cmbPeriodo"));
        WebElement uf = driver.findElement(By.id("cmbUF"));
        WebElement municipio = driver.findElement(By.id("cmbMunicipio"));

        System.out.println("1: " + ano.getText());
        System.out.println("2: " + periodo.getText());
        System.out.println("3: " + uf.getText());

        Select selectAno = new Select(ano);
        Select selectPeriodo = new Select(periodo);
        Select selectUf = new Select(uf);
        Select selectMunicipio = new Select(municipio);

        List<WebElement> listaAnos = selectAno.getOptions();
        List<WebElement> listaPeriodos = selectPeriodo.getOptions();
        List<WebElement> listaEstados = selectUf.getOptions();
        // List<WebElement> listaMunicipios = selectMunicipio.getOptions();

        System.out.println("anos: " + listaAnos.size());
        System.out.println("períodos: " + listaPeriodos.size());
        System.out.println("estados: " + listaEstados.size());

        for(int i = listaAnos.size() - 1; i >= 0; i--) {
            System.out.println("Ano " + i + ": " + listaAnos.get(i).getText());
        }

        for(int j = 0; j < listaPeriodos.size(); j++) {
            System.out.println("Período " + j + ": " + listaPeriodos.get(j).getText());
        }

        for(int k = 0; k < listaEstados.size(); k++) {
            System.out.println("Estado " + k + ": " + listaEstados.get(k).getText());
        }

        selectAno.selectByIndex(3);
        selectPeriodo.selectByIndex(0);
        selectUf.selectByIndex(0);

        System.out.println("4: " + municipio.getText());

        List<WebElement> listaMunicipios = selectMunicipio.getOptions();
        System.out.println("municípios: " + listaMunicipios.size());
        for(int l = 0; l < listaMunicipios.size(); l++) {
            System.out.println("Município " + l + ": " + listaMunicipios.get(l).getText());
        }

        selectMunicipio.selectByIndex(0);
    }
}

//        Map<String, List<String>> municipiosPorEstado = new LinkedHashMap<>();
//
//        selectAno.selectByIndex(3);

//        for(int e = 0; e < listaEstados.size(); e++) {
//            String estado = listaEstados.get(e).getText();
//            List<String> municipios = new ArrayList<>();
//
//            for(WebElement we : listaMunicipios) municipios.add(we.getText());
//
//            municipiosPorEstado.put(estado, municipios);
//        }

// for(String e : municipiosPorEstado.keySet()) System.out.println(e + " -> " + municipiosPorEstado.get(e).size());

//        for(int i = listaAnos.size() - 1; i >= 0; i--) {
//            selectAno.selectByIndex(i);
//            for(int j = 0; j < listaPeriodos.size(); j++) {
//                selectPeriodo.selectByIndex(j);
//                for(int k = 0; k < listaEstados.size(); k++) {
//                    selectUf.selectByIndex(k);
//                    List<WebElement> listaMunicipios = selectMunicipio.getOptions();
//                    for(int l = 0; l < listaMunicipios.size(); l++) {
//                        System.out.println(listaMunicipios.get(l));
//                    }
//                    submitButton.click();
//                }
//            }
//        }

//        // análise da tabela
//        WebElement findTable = driver.findElement(By.tagName("table"));
//        List<WebElement> trs = findTable.findElements(By.tagName("tr"));
//
//        List<WebElement> tdsCorrente = trs.get(2).findElements(By.tagName("td"));
//        String uniaoCorrente = tdsCorrente.get(4).getText();
//        String totalCorrente = tdsCorrente.get(10).getText();
//
//        List<WebElement> tdsCapital = trs.get(3).findElements(By.tagName("td"));
//        String uniaoCapital = tdsCapital.get(3).getText();
//        String totalCapital = tdsCapital.get(9).getText();
//
//        List<WebElement> tdsTotal = trs.get(16).findElements(By.tagName("td"));
//        String total = tdsTotal.get(3).getText();