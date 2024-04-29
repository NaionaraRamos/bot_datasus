package org.example;

import net.bytebuddy.dynamic.scaffold.MethodGraph;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        final String URL = "http://siops.datasus.gov.br/filtro_rel_ges_dt_municipal.php";

        Map<String, String> mapeamentoCodigosEstados = new LinkedHashMap<>();
        Map<String, String> mapeamentoCodigosMunicipios = new LinkedHashMap<>();
        Map<String, String> mapeamentoCodigosPeriodos = new LinkedHashMap<>();
        //Set<WebElement> listaTotalMunicipios = new LinkedHashSet<>();

        WebDriver driver = new ChromeDriver();
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        WebElement webElementPeriodo = driver.findElement(By.id("cmbPeriodo"));
        Select selectPeriodo = new Select(webElementPeriodo);
        List<WebElement> listaPeriodos = selectPeriodo.getOptions();

        for(WebElement we : listaPeriodos) {
            mapeamentoCodigosPeriodos.put(we.getAttribute("value"), we.getText());
        }

        WebElement webElementUf = driver.findElement(By.id("cmbUF"));
        Select selectUf = new Select(webElementUf);
        List<WebElement> listaUfs = selectUf.getOptions();

        for(int i = 0; i < listaUfs.size(); i++) {
            mapeamentoCodigosEstados.put(listaUfs.get(i).getAttribute("value"), listaUfs.get(i).getText());
        }

        WebElement webElementMunicipio;
        Select selectMunicipio;
        List<WebElement> listaMunicipios;

        // para poder popular a chave do Acre. Quando a página é aberta, embora o Acre seja o primeiro estado,
        // a caixa de seleção de municípios não é populada. Assim, é preciso selecionar outro elemento e voltar ao Acre
        // ppara que a caixa seja populada.
        webElementUf = driver.findElement(By.id("cmbUF"));
        selectUf = new Select(webElementUf);
        selectUf.selectByIndex(1);

        for(int uf = 0; uf < 1; uf++) {
            WebElement elUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
            selectUf = new Select(elUf);
            selectUf.selectByIndex(uf);

            webElementMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio")));
            selectMunicipio = new Select(webElementMunicipio);
            listaMunicipios = selectMunicipio.getOptions();

            for(WebElement we : listaMunicipios) mapeamentoCodigosMunicipios.put(we.getAttribute("value"), we.getText());
        }

        String codigoEstado = "0", nomeEstado = "";

        int numeroLinha = 1;
        Map<Integer, Object[]> dadosTabela = new TreeMap<>();
        dadosTabela.put(numeroLinha++, new Object[]{"UF", "IBGE", "Município", "União corrente", "União capital", "Total atenção básica corrente", "Total atenção básica capital"});

        for (String codigoMunicipio : mapeamentoCodigosMunicipios.keySet()) {

            if(!codigoEstado.startsWith(codigoMunicipio.substring(0, 2))) {
                codigoEstado = codigoMunicipio.substring(0, 2);
                nomeEstado = mapeamentoCodigosEstados.get(codigoEstado);
            }

            String nomeMunicipio = mapeamentoCodigosMunicipios.get(codigoMunicipio);

            StringBuilder url = new StringBuilder(URL).append("?S=1&UF=").append(codigoEstado).append(";&Municipio=").append(codigoMunicipio).append(";&Ano=2020&Periodo=12");
            driver.get(url.toString());

            WebElement submitButton = driver.findElement(By.name("BtConsultar"));
            submitButton.click();

            // chamar método de inserção das informações em uma lista
            String ausenciaDeDados = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(1).findElements(By.tagName("td")).get(0).getText();

             if(ausenciaDeDados.equals("Subfunções")) {
                 String uniaoCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(4).getText();
                 String totalCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(10).getText();
                 String uniaoCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(3).getText();
                 String totalCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(9).getText();
                 dadosTabela.put(numeroLinha++, new Object[]{nomeEstado, codigoMunicipio, nomeMunicipio, uniaoCorrente, uniaoCapital, totalCorrente, totalCapital});
            } else {
                 dadosTabela.put(numeroLinha++, new Object[]{nomeEstado, codigoMunicipio, nomeMunicipio, ausenciaDeDados});
            }

            driver.navigate().back();
        }

        driver.close();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("1º Bimestre");
        Row dadosMunicipios;
        int idColuna = 0, idLinha = 0;

        for (Integer key : dadosTabela.keySet()) {
            dadosMunicipios = sheet.createRow(idColuna++);
            Object[] objectArr = dadosTabela.get(key);
            int cellId = 0;

            if(objectArr.length == 4) {
                sheet.addMergedRegion(CellRangeAddress.valueOf("D" + (idLinha + 1) + ":G" + (idLinha + 1)));
                for(Object obj : objectArr) {
                    Cell cell = dadosMunicipios.createCell(cellId++);
                    cell.setCellValue((String) obj);
                }
            } else {
                for(Object obj : objectArr) {
                    Cell cell = dadosMunicipios.createCell(cellId++);
                    cell.setCellValue((String)obj);
                }
            }
            idLinha++;
        }

        for(int i = 0; i < idColuna; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream out = new FileOutputStream("C:/Users/Naionara Ramos/Projetos/robot/planilhas/2020.xlsx");
        workbook.write(out);
        out.close();

        System.exit(0);

        // testar com um bimestre para ver a diferença.
        // Pontos a serem considerados: como fazer loop pelos períodos sem aninhar muitos loops.
        // condição dos anos posteriores a 2020, quando há uma coluna a mais na tabela.
        // divisão dos vários loops em métodos.

        // pensar na criação de algo do tipo: Map<String, List<String>> mapeandoUrlsAosDados ...,
        //    para jogar as bases das urls e as informações já existentes: UF, codigo IBGE, nome município...
        //    e percorrer as urls em um método à parte.
    }

    /*public static void main(String[] args) throws IOException {

        // gerar a planilha do 1ºbimestre de 2020 para todos os municípios demorou 2 horas e 40 minutos.

        final String URL = "http://siops.datasus.gov.br/filtro_rel_ges_dt_municipal.php";

        WebDriver driver = new ChromeDriver();
        driver.get(URL);
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        WebElement webElementAno = driver.findElement(By.id("cmbAno"));
        Select selectAno = new Select(webElementAno);
        List<WebElement> listaAnos = selectAno.getOptions();

        for(int ano = listaAnos.size() - 1; ano >= 3; ano--) {

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

            for (int periodo = 0; periodo < 1; periodo++) {
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

                for(int uf = 0; uf < listaUfs.size(); uf++) {

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

                    for(int municipio = 0; municipio < listaMunicipios.size(); municipio++) {

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

                        webElementPeriodo = driver.findElement(By.id("cmbPeriodo"));
                        selectPeriodo = new Select(webElementPeriodo);
                        selectPeriodo.selectByIndex(periodo);
                    }
                }

                for(Integer key : dadosTabela.keySet()) {
                    dadosMunicipios = sheet.createRow(idColuna++);
                    //sheet.autoSizeColumn(idColuna);
                    Object[] objectArr = dadosTabela.get(key);
                    int cellId = 0;

                    for(Object obj : objectArr) {
                        Cell cell = dadosMunicipios.createCell(cellId++);
                        sheet.autoSizeColumn(idColuna);
                        cell.setCellValue((String)obj);
                    }
                }

                for(int i = 0; i < idColuna; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            FileOutputStream out = new FileOutputStream("C:/Users/Naionara Ramos/Projetos/robot/planilhas/" + stringAno + ".xlsx");
            workbook.write(out);
            out.close();
        }

        driver.close();
        System.exit(0);
    }*/

//    private Map<Integer, Object[]> montagemListagemDados() {
//
//    }

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

}