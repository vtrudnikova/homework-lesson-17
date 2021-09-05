package com.tricentis.demowebshop;

import com.codeborne.selenide.WebDriverRunner;
import com.github.javafaker.Faker;
import io.restassured.http.Cookies;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.open;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DemoWebShopTests {
    Faker faker = new Faker();
    String baseUrl = "http://demowebshop.tricentis.com/";

    @Test
    public void addItemToCart() {
        String email = "nayebruzebru-6935@yopmail.com";
        String password = "Qwerty123";

        Response response = given()
                .baseUri(baseUrl)
                .basePath("/login")
                .contentType("application/x-www-form-urlencoded")
                .formParam("Email", email)
                .formParam("Password", password)
                .formParam("RememberMe", false)
                .post();
        response.then().statusCode(302);
        Cookies cookies = response.detailedCookies();

        Response responseAddCart = given()
                .baseUri(baseUrl)
                .basePath("/addproducttocart/catalog/45/1/1")
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .cookies(cookies)
                .post();
        responseAddCart.then().statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("The product has been added to your <a href=\"/cart\">shopping cart</a>"));
        String json = responseAddCart.asString();
        String updatetopcartsectionhtml = JsonPath.from(json).getString("updatetopcartsectionhtml");

        open("http://demowebshop.tricentis.com");

        for (io.restassured.http.Cookie cookieFromResponse : cookies) {
            Cookie cookieInSeleniumFormat = new Cookie(cookieFromResponse.getName(), cookieFromResponse.getValue(),
                    cookieFromResponse.getDomain(), cookieFromResponse.getPath(), cookieFromResponse.getExpiryDate());
            WebDriverRunner.getWebDriver().manage().addCookie(cookieInSeleniumFormat);
        }
        open("http://demowebshop.tricentis.com/");
        String items = $x("//*[@class='cart-qty']").getText();
        assertThat(items).isEqualTo(updatetopcartsectionhtml);
    }

    @Test
    public void notSubscribeWithEmptyEmail() {
        Response response = given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("email=")
                .post("http://demowebshop.tricentis.com/subscribenewsletter");
        response.then().statusCode(200);
        response.then().body("Success", equalTo(false));
        response.then().body("Result", equalTo("Enter valid email"));
    }

    @Test
    public void successfulSubscribeWithValidEmail() {
        String email = faker.internet().emailAddress();
        Response response = given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body(String.format("email=%s", email))
                .post("http://demowebshop.tricentis.com/subscribenewsletter");
        response.then().statusCode(200);
        response.then().body("Success", equalTo(true));
        response.then().body("Result", equalTo("Thank you for signing up! A verification" +
                " email has been sent. We appreciate your interest."));
    }
}