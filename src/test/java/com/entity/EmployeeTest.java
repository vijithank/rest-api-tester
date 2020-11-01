package com.entity;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utility.MockApi;
import utility.RetryAnalyser;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.*;

public class EmployeeTest {

    private static final String baseURI = "http://dummy.restapiexample.com/api/v1";
    RequestSpecification requestSpecification;
    Employee employee;

    private MockApi mockApi;

    @BeforeClass
    public void setupSpec() {
        mockApi = new MockApi();
    }

    @BeforeMethod
    public void setUp() {
        employee = new Employee("samson", 12, 20000);
        RestAssured.baseURI = baseURI;
        requestSpecification = given();

    }

    /**
     *  RetryAnalyser.class is implemented to run 2 times when test fails,
     *  as "http://dummy.restapiexample.com/api/v1" not seems to be stable.
     * @param context is used to set user id that can to global context so it can be used in other tests
     */
    @Test(priority = 1, retryAnalyzer = RetryAnalyser.class)
    void testCreateEmployee(ITestContext context) {
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(employee).
                        when()
                        .post("/create").
                        then()
                        .statusCode(HttpStatus.SC_OK).
                        and()
                        .body("data.id", notNullValue())
                        .body("data.name", equalTo(employee.getName()))
                        .body("data.salary", equalTo(employee.getSalary()))
                        .body("data.age", equalTo(employee.getAge()))
                        .extract().response();
        JsonPath responseJson = response.jsonPath();
        context.setAttribute("employeeId", responseJson.get("data.id"));

    }

    @Test(priority = 2, dependsOnMethods = "testCreateEmployee")
    void testGetEmployee(ITestContext context) {
        employee.setId(Integer.parseInt(context.getAttribute("employeeId").toString()));
        when()
                .get("/employee/" + employee.getId()).
                then()
                .statusCode(HttpStatus.SC_OK).
                and()
                .body("data.id", equalTo(employee.getId()))
                .body("data.employee_name", equalTo(employee.getName()))
                .body("data.employee_salary", equalTo(employee.getSalary()))
                .body("data.employee_age", equalTo(employee.getAge()));

    }

    @Test(priority = 3, dependsOnMethods = "testCreateEmployee")
    void testDeleteEmployee(ITestContext context) {
        String employeeId = context.getAttribute("employeeId").toString();
        when()
                .delete("/delete/" + employeeId).
                then()
                .statusCode(HttpStatus.SC_OK).
                and()
                .body("status", equalTo("success"))
                .body("message", equalTo("successfully! deleted Records"));
    }

    @Test(priority = 4, dependsOnMethods = "testDeleteEmployee")
    void testGetDeletedEmployee(ITestContext context) {
        String employeeId = context.getAttribute("employeeId").toString();
        when()
                .get("/employee/" + employeeId).
                then()
                .statusCode(HttpStatus.SC_OK).
                and()
                .body("data", nullValue());

    }

    @Test(priority = 5)
    void testGetErroneousEmployee() {
        mockApi.stubFailedGet();
        RestAssured.baseURI = "http://localhost:2020";
        RestAssured.given()
                .when().log().ifValidationFails()
                .get("/non-existing-api").
                then()
                .statusCode(200);

    }

    @AfterClass
    public static void tearDown() {
        MockApi.tearDown();
    }


}
