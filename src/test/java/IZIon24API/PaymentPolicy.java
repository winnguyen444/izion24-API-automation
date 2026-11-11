package IZIon24API;

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class PaymentPolicy {
	// SIT https://api.dev2.izion24.com.vn
	// PREPROD https://api.preprod.izion24.com.vn
	// UAT https://api.uat2.izion24.com.vn
	//String urlString = "https://api.preprod.izion24.com.vn";
	
	Map<String, String> ENVS = Map.of(
		"SIT", "https://api.dev2.izion24.com.vn",
		"PREPROD", "https://api.preprod.izion24.com.vn",
		"UAT", "https://api.uat2.izion24.com.vn"
);
	
	String env = System.getProperty("env", "UAT").toUpperCase();
	String urlString = ENVS.get(env);
	String authTokenString;
	
	
	@BeforeClass
	public void LoginGetToken() {
		Map<String, Object> loginBody = new HashMap<>();
		loginBody.put("channelPartner", "payoo");
		loginBody.put("password", "CcQ8cG76aOiJJUH5ZIFK");
		loginBody.put("userName", "payoo_user");
		
		
		Response res = given()
				.contentType(ContentType.JSON)
				.body(loginBody)
			.when()
				.post(urlString + "/partner/login")
			.then()
				.statusCode(200)
				.extract().response();
		
		String tokenString =  res.jsonPath().getString("data.accessToken");
		Assert.assertNotNull(tokenString, "Không lấy được token, response: " + res.asString());
		authTokenString = "Bearer " + tokenString;		
		System.out.println(authTokenString);
	}
	
	
	// Danh sách billId cần thanh toán - thêm/bớt tại đây
	// Muốn chạy song song thì đổi thành: @DataProvider(name = "billIds", parallel = true)
	@DataProvider(name = "billIds")
	public Object[][] billIds() {
		return new Object[][] {
			{ "STG20252350" },
//			{ "UAT04250136" },
//			{ "UAT04250137" }
		};
	}
	
	
	@Test(dataProvider = "billIds")
	public void getBillAndPayment(String billId) {
		
		//GET API 
		
		Map<String, Object> billBody = new HashMap<String, Object>();
		billBody.put("billId", billId);
		
		Response bill = given()
				.header("Authorization", authTokenString)
				.contentType(ContentType.JSON)
				.body(billBody)
			.when()
				.post(urlString + "/partner/payoo/get-bill-data")
			.then()
				.statusCode(200)
				.extract().response();
		JsonPath json = bill.jsonPath();
		
		//GET API PAYMENT
		Map<String, Object> confirmBody = new HashMap<>();
		confirmBody.put("billId", billId);
		confirmBody.put("transactionId", json.getString("id"));
		confirmBody.put("paidAmount", json.getString("data.billInfo.cashAmount"));
		confirmBody.put("paymentDate", json.getString("data.billInfo.orderDate"));
		
		Response confirm = given()
					.header("Authorization", authTokenString)
					.log().all()
					.contentType(ContentType.JSON)
					.body(confirmBody)
					.log().all()
					.log().ifValidationFails()
				.when()
					.post(urlString + "/partner/payoo/cash-payment-confirm")
				.then()
					.statusCode(200)
					.log().ifValidationFails()
					.log().all()
					.extract().response();
		
		System.out.println("Status: " + confirm.statusCode());
	    System.out.println("Body: " + confirm.asString());		
	}
	
	
}
