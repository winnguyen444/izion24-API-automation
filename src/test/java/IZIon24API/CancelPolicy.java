package IZIon24API;

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class CancelPolicy {
	String urlString = "https://api.uat2.izion24.com.vn";
	String authTokenString;

	// Danh sách proposalNumber cần hủy: thêm/bớt mã ở đây
	@DataProvider(name = "proposalList")
	public Object[][] proposalList() {
		return new Object[][] { { "IZIPL0000004659" }, { "PFEMOTOR0000017414" } };
	}

	// Login 1 lần duy nhất trước khi chạy các test
	@BeforeClass
	public void LoginGetToken() {
		Map<String, Object> loginBody = new HashMap<>();
		loginBody.put("channelPartner", "crm");
		loginBody.put("userName", "crm_user");
		loginBody.put("password", "5Xpk0fwGFgd11SP1T7Xy");

		Response res = 
			given()
				.contentType(ContentType.JSON)
				.body(loginBody)
			.when()
				.post(urlString + "/partner/login")
			.then()
				.statusCode(200)
				.extract().response();

		String tokenString = res.jsonPath().getString("data.accessToken");
		Assert.assertNotNull(tokenString, "Không lấy được token, response: " + res.asString());
		authTokenString = "Bearer " + tokenString;
		System.out.println(authTokenString);
	}

	@Test(dataProvider = "proposalList")
	public void cancelPolicy(String proposalNumber) {
		System.out.println("===== Hủy proposal: " + proposalNumber + " =====");
		cancelRegister(proposalNumber);
		cancelFinalize(proposalNumber);
	}

	private void cancelRegister(String proposalNumber) {

		Map<String, Object> RegisterBody = new HashMap<String, Object>();
		RegisterBody.put("proposalNumber", proposalNumber);
		RegisterBody.put("requestedBy", "vvgiap");
		RegisterBody.put("cancellationRequestor", "CUSTOMER");

		Response res = 
			given()
				.header("Authorization", authTokenString)
				.contentType(ContentType.JSON).body(RegisterBody)
			.when()
				.post(urlString + "/partner/crm/policy/cancel-register")
			.then()
				.statusCode(200)
				.extract().response();
		System.out.println("Status: " + res.statusCode());
		System.out.println("Body: " + res.asString());
	}

	private void cancelFinalize(String proposalNumber) {
		Map<String, Object> FinalizeBody = new HashMap<>();
		FinalizeBody.put("cancellationDate", "2026-04-06T19:00:00+07:00");
		FinalizeBody.put("cancellationReason", "User request for cancel");
		FinalizeBody.put("proposalNumber", proposalNumber);
		FinalizeBody.put("refundAmount", "50000");
		FinalizeBody.put("refundChannel", "PARTNER");
		FinalizeBody.put("requestedBy", "vvgiap");
		FinalizeBody.put("status", "success");

		Response res = 
			given()
				.header("Authorization", authTokenString)
				.contentType(ContentType.JSON)
				.body(FinalizeBody)
				.log().ifValidationFails()
			.when()
				.post(urlString + "/partner/crm/policy/cancel-finalize")
			.then()
				.statusCode(200)
				.log().ifValidationFails()
				.extract().response();

		System.out.println("Status: " + res.statusCode());
		System.out.println("Body: " + res.asString());
	}
}
