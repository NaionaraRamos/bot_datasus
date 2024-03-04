package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        final String URL = "http://siops.datasus.gov.br/filtro_rel_ges_dt_municipal.php";

        WebDriver driver = new ChromeDriver();
        driver.get(URL);
        driver.manage().window().maximize();

        // ano
        WebElement ano = driver.findElement(By.name("cmbAno"));
        Select selectAno = new Select(ano);
        selectAno.selectByIndex(3); // seleciona 2020

        // período
        WebElement periodo = driver.findElement(By.name("cmbPeriodo"));
        Select selectPeriodo = new Select(periodo);
        //selectPeriodo.selectByIndex(0);

        // uf
        WebElement uf = driver.findElement(By.name("cmbUF"));
        Select selectUf = new Select(uf);
        List<WebElement> listaEstados = selectUf.getOptions();
        //for(WebElement estado : estados) System.out.println("Estado: " + estado.getText());
        //selectUf.selectByIndex(1);

        // município
        WebElement municipio = driver.findElement(By.name("cmbMunicipio[]"));
        Select selectMunicipio = new Select(municipio);
        List<WebElement> listaMunicipios = selectMunicipio.getOptions();
        //for(WebElement cidade : listaMunicipios) System.out.println("Município: " + cidade.getText());
        //selectMunicipio.selectByIndex(0);

        // clicar em consultar
        WebElement submitButton = driver.findElement(By.name("BtConsultar"));
        submitButton.click();

        // análise da tabela
        WebElement findTable = driver.findElement(By.tagName("table"));
        List<WebElement> trs = findTable.findElements(By.tagName("tr"));

        List<WebElement> tdsCorrente = trs.get(2).findElements(By.tagName("td"));
        String uniaoCorrente = tdsCorrente.get(4).getText();
        String totalCorrente = tdsCorrente.get(10).getText();
        //System.out.println("corrente: " + uniaoCorrente + ", total: " + totalCorrente);

        List<WebElement> tdsCapital = trs.get(3).findElements(By.tagName("td"));
        String uniaoCapital = tdsCapital.get(3).getText();
        String totalCapital = tdsCapital.get(9).getText();
        //System.out.println("capital: " + uniaoCapital + ", total: " + totalCapital);

        List<WebElement> tdsTotal = trs.get(16).findElements(By.tagName("td"));
        String total = tdsTotal.get(3).getText();
        //System.out.println("Total: " + total);

        // modo alternativo, mais curto
        /**
         * String uniaoCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(4).getText();
         * String totalCorrente = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(2).findElements(By.tagName("td")).get(10).getText();
         * String uniaoCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(3).getText();
         * String totalCapital = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(3).findElements(By.tagName("td")).get(9).getText();
         * String total = driver.findElement(By.tagName("table")).findElements(By.tagName("tr")).get(16).findElements(By.tagName("td")).get(3).getText();
         */

        // voltar
        //driver.navigate().back();
    }
}
