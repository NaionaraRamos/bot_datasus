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
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    // esse mapeamento é de um para um. Talvez fosse melhor utilizar um Pair em vez de um mapa.
    private static Map<String, String> getStatesCodes(WebDriver driver) {
        Map<String, String> statesCodesMap = new TreeMap<>();

        WebElement webElementUF = driver.findElement(By.id("cmbUF"));
        Select selectUF = new Select(webElementUF);
        List<WebElement> listaUFs = selectUF.getOptions();
        
        for(int uf = 0; uf < listaUFs.size(); uf++) {
            String stateName = listaUFs.get(uf).getText();
            String stateCode = listaUFs.get(uf).getAttribute("value");
            System.out.println(stateName + " - " + stateCode);
            statesCodesMap.put(stateName, stateCode);

            // selectUF = new Select(webElementUF);
            // selectUF.selectByIndex(1);

            //WebElement elUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
            //selectUf = new Select(elUf);
            //selectUF.selectByIndex(uf);

           // webElementMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio")));
            //selectMunicipio = new Select(webElementMunicipio);
            //listaMunicipios = selectMunicipio.getOptions();

        }
        
        return statesCodesMap;
    }

    private static Map<String, String> getCitiesCodes(WebDriver driver) {
        Map<String, String> citiesCodesMap = new TreeMap<>();

        WebElement webElementMunicipio = driver.findElement(By.id("cmbMunicipio"));
        Select selectMunicipio = new Select(webElementMunicipio);
        List<WebElement> listaMunicipios = selectMunicipio.getOptions();
        for(int city = 0; city < listaMunicipios.size(); city++) {
            String cityName = listaMunicipios.get(city).getText();
            String cityCode = listaMunicipios.get(city).getAttribute("value");
            System.out.println(cityName + " - " + cityCode);
            citiesCodesMap.put(cityName, cityCode);
        }
        return citiesCodesMap;
    }

    private static void writeItems(String filename, List<String> itemsList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".txt"))) {
            for (String item : itemsList) {
                bw.write(item);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // private static void testMap(WebDriver driver) {
        
    //     WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
    //     Map<String, List<String>> mappingCitiesToState = new LinkedHashMap<>();

    //     WebElement webElementUf = driver.findElement(By.id("cmbUF"));
    //     Select selectUf = new Select(webElementUf);
    //     List<WebElement> listaUfs = selectUf.getOptions();

    //     WebElement webElementMunicipio;
    //     Select selectMunicipio;
    //     List<WebElement> listaMunicipios;

    //     // webElementUf = driver.findElement(By.id("cmbUF"));
    //     //selectUf = new Select(webElementUf);
    //     selectUf.selectByIndex(1);
    //    // String ufCode = "";

    //     //for(int uf = 0; uf < 4; uf++) {
    //     //for(int i = 0; i < listaUfs.size(); i++) {
    //     for(WebElement uf : listaUfs) {
    //         //ufCode = listaUfs.get(i).getAttribute("value");

    //         //WebElement elUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
    //        // selectUf = new Select(elUf);
    //         //selectUf.selectByIndex(i);
    //         String ufCode = uf.getAttribute("value");

    //         webElementMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio")));
    //         selectMunicipio = new Select(webElementMunicipio);
    //         listaMunicipios = selectMunicipio.getOptions();
    //         List<String> citiesCodes = new ArrayList<>();

    //         for(WebElement we : listaMunicipios) {
    //             String cityCode = we.getAttribute("value");
    //             citiesCodes.add(cityCode);
    //         }

    //         // int citiesLen = citiesCodes.size();
    //         // System.out.println(citiesLen);
    //         mappingCitiesToState.put(ufCode, citiesCodes);
    //     }
    // }

    private static Map<String, List<String>> mapeamentoMunicipiosParaEstados(WebDriver driver) {
        Map<String, List<String>> mapeamentoCidadesParaEstados = new TreeMap<>();
        
        WebElement webElementUF = driver.findElement(By.id("cmbUF"));
        Select selectUF = new Select(webElementUF);
        //selectUF.selectByIndex(1);
        List<WebElement> listaUFs = selectUF.getOptions();
        selectUF.selectByIndex(1);
        
        for(int uf = 0; uf < 3; uf++) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
            WebElement elUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
            //selectUF = new Select(elUf);
            selectUF.selectByIndex(uf);
            String ufValue = listaUFs.get(uf).getText();  //listaUFs.get(uf).getAttribute("value");
            
            WebElement webElementMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio"))); //driver.findElement(By.id("cmbMunicipio"));
            Select selectMunicipio = new Select(webElementMunicipio);
            List<WebElement> listaMunicipios = selectMunicipio.getOptions();
            List<String> listaCodigosCidades = new ArrayList<>();

            for(int city = 0; city < listaMunicipios.size(); city++) {
                String cityValue = listaMunicipios.get(city).getAttribute("value");
                listaCodigosCidades.add(cityValue);
            }
            mapeamentoCidadesParaEstados.put(ufValue, listaCodigosCidades);
        }

        // for(int uf = 0; uf < 1; uf++) {
        //     WebElement elUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
        //     selectUf = new Select(elUf);
        //     selectUf.selectByIndex(uf);

        //     webElementMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio")));
        //     selectMunicipio = new Select(webElementMunicipio);
        //     listaMunicipios = selectMunicipio.getOptions();

        //     for(WebElement we : listaMunicipios) mapeamentoCodigosMunicipios.put(we.getAttribute("value"), we.getText());
        // }

        return mapeamentoCidadesParaEstados;
    }

    private static void writeMapItems(String filename, Map<String, List<String>> mapItemsList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".txt"))) {
            for (String item : mapItemsList.keySet()) {
                bw.write("----------" + item + "----------");
                List<String> subItems = mapItemsList.get(item);
                for(String subItem : subItems) {
                    bw.write(subItem);
                    bw.newLine();
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, List<String>> newTestMap(WebDriver driver) {
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        WebElement webElementUf = driver.findElement(By.id("cmbUF"));
        Select selectUf = new Select(webElementUf);
        List<WebElement> listaUfs = selectUf.getOptions();
        List<String> ufsCodes = new LinkedList<>();

        for(WebElement uf : listaUfs) {
            ufsCodes.add(uf.getAttribute("value"));
        }
        
        Map<String, List<String>> mappingCitiesToState = new LinkedHashMap<>();
        WebElement webElementMunicipio;
        Select selectMunicipio;
        List<WebElement> listaMunicipios;

        webElementUf = driver.findElement(By.id("cmbUF"));
        selectUf = new Select(webElementUf);
        selectUf.selectByIndex(1);

        for(int uf = 0; uf < ufsCodes.size(); uf++) {
            
            String ufCode = ufsCodes.get(uf);
            List<String> citiesCodes = new ArrayList<>(); 
            webElementUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
            selectUf = new Select(webElementUf);
            selectUf.selectByIndex(uf);

            webElementMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio")));
            selectMunicipio = new Select(webElementMunicipio);
            listaMunicipios = selectMunicipio.getOptions();

            for(WebElement we : listaMunicipios) {
                String cityCode = we.getAttribute("value");
                citiesCodes.add(cityCode);
            }
            
            mappingCitiesToState.put(ufCode, citiesCodes);
        }

        return mappingCitiesToState;
    }

    private static List<String> formBasicUrls(WebDriver driver) {

        List<String> stateCityUrls = new ArrayList<>();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        WebElement webElementUf = driver.findElement(By.id("cmbUF"));
        Select selectUf = new Select(webElementUf);
        List<WebElement> listaUfs = selectUf.getOptions();
        List<String> ufsCodes = new LinkedList<>();

        for(WebElement uf : listaUfs) {
            ufsCodes.add(uf.getAttribute("value"));
        }
        
        WebElement webElementMunicipio;
        Select selectMunicipio;
        List<WebElement> listaMunicipios;

        webElementUf = driver.findElement(By.id("cmbUF"));
        selectUf = new Select(webElementUf);
        selectUf.selectByIndex(1);

        for(int uf = 0; uf < ufsCodes.size(); uf++) {
            
            String ufCode = ufsCodes.get(uf);
            webElementUf = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbUF")));
            selectUf = new Select(webElementUf);
            selectUf.selectByIndex(uf);

            webElementMunicipio = wait.until(ExpectedConditions.elementToBeClickable(By.id("cmbMunicipio")));
            selectMunicipio = new Select(webElementMunicipio);
            listaMunicipios = selectMunicipio.getOptions();

            for(WebElement we : listaMunicipios) {
                String cityCode = we.getAttribute("value");
                StringBuilder basicUrl = new StringBuilder("http://siops.datasus.gov.br/consleirespfiscal.php?S=1&UF=").append(ufCode).append(";&Municipio=").append(cityCode); //;&Ano=2021&Periodo=2";
                stateCityUrls.add(basicUrl.toString());
            }
        }

        return stateCityUrls;
    }

    private static List<String> readItems(String filename) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<String> basicUrls = new ArrayList<>();
        String st;
        while((st = br.readLine()) != null) {
            basicUrls.add(st);
        }
        br.close();
        return basicUrls;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        //final String URL = "https://siops.datasus.gov.br/consleirespfiscal.php?S=1&UF=12;&Municipio=120001;&Ano=2020&Periodo=2";
        // driver.get(URL);
        // WebElement submitButton = driver.findElement(By.name("BtConsultar"));
        // submitButton.click();
        // driver.navigate().back();

        WebDriver driver = new Main().getWebDriver(args);
        
        // para criar as urls base, com UF e Município
        // List<String> mapping = formBasicUrls(driver);
        // writeItems("URLS_cidades", mapping);

        // para ler as urls base
        List<String> basicUrls = readItems("URLS_cidades.txt");
        System.out.println("size: " + basicUrls.size());

        int ano = 2016; // vai até 2025
        int periodo = 2; // 12 - 1º bimestre; 14 - 2º bimestre; 1 - 3º bimestre; 18 - 4º bimestre; 20 - 5º bimestre; 2 - 6º bimestre

        for (String url : basicUrls) {
            url = url + ";&Ano=" + ano + ";&Periodo=" + periodo;

            driver.get(url);
            WebElement submitButton = driver.findElement(By.name("BtConsultar"));
            submitButton.click();
            driver.navigate().back();
        }

        driver.close();
    }

    /*public static void main(String[] args) throws IOException, InterruptedException {

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
*/
    
// Escolha de navegador via argumento (ex: "chromium" ou "chrome").
    // Se nenhum argumento for passado, usa Chromium por padrão.
    private WebDriver getWebDriver(String[] args) {
        String browser = (args.length > 0) ? args[0].trim().toLowerCase() : "chromium";

        WebDriver driver;
        switch (browser) {
            case "chromium" -> {
                String chromiumBinary = System.getenv().getOrDefault("CHROMIUM_BIN", "/var/lib/snapd/snap/bin/chromium");
                String chromedriverBinary = System.getenv("CHROMEDRIVER_BIN");

                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setBinary(chromiumBinary);

                // O Chromium snap costuma falhar com sandbox e espaços de memória.
                // Forçar um perfil temporário também ajuda quando o navegador não consegue criar pastas no snap.
                String tmpProfile = System.getProperty("java.io.tmpdir") + "/selenium-profile-" + UUID.randomUUID();
                chromeOptions.addArguments(
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--disable-extensions",
                        "--disable-software-rasterizer",
                        "--remote-debugging-port=9222",
                        "--user-data-dir=" + tmpProfile
                );

                if (chromedriverBinary != null && !chromedriverBinary.isBlank()) {
                    System.setProperty("webdriver.chrome.driver", chromedriverBinary);
                } else {
                    // Ajustar a versão do chromedriver para a versão do Chromium instalado.
                    String chromeVersion = detectChromiumVersion(chromiumBinary);
                    if (chromeVersion != null) {
                        String majorVersion = chromeVersion.split("\\.")[0];
                        System.out.println("Detected Chromium version: " + chromeVersion + " (major=" + majorVersion + ")");
                        try {
                            WebDriverManager.chromedriver().browserVersion(majorVersion).setup();
                        } catch (Exception firstTry) {
                            // fallback para tentar usar a versão exata completa
                            WebDriverManager.chromedriver().driverVersion(chromeVersion).setup();
                        }
                    } else {
                        WebDriverManager.chromedriver().setup();
                    }
                }

                driver = new ChromeDriver(chromeOptions);
            }
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                driver = new ChromeDriver(chromeOptions);
            }
            case "firefox", "ff" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                driver = new FirefoxDriver(firefoxOptions);
            }
            default -> throw new IllegalArgumentException("Navegador desconhecido: " + browser + ". Use 'firefox', 'chromium' ou 'chrome'.");
        }

        return driver;
    }

    private static String detectChromiumVersion(String chromiumBinary) {
        try {
            Process process = new ProcessBuilder(chromiumBinary, "--product-version").start();
            String version;
            try (Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A")) {
                version = scanner.hasNext() ? scanner.next().trim() : null;
            }
            process.waitFor();
            return (version != null && !version.isBlank()) ? version : null;
        } catch (Exception e) {
            return null;
        }
    }
}
