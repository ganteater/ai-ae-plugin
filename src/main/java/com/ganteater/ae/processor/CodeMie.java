package com.ganteater.ae.processor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import com.ganteater.ae.CommandException;
import com.ganteater.ae.processor.annotation.CommandDescription;
import com.ganteater.ae.processor.annotation.CommandExamples;
import com.ganteater.ae.util.xml.easyparser.Node;

/**
 * OpenAI-compatible processor that authenticates against CodeMie and delegates calls to {@link OpenAI}.
 *
 * <p>
 * The processor exchanges a {@code username}/{@code password} pair for an access token and then configures the base
 * OpenAI implementation with:
 * </p>
 * <ul>
 *   <li>{@code apiKey}: the retrieved access token</li>
 *   <li>{@code baseUrl}: CodeMie OpenAI-compatible endpoint</li>
 * </ul>
 */
public class CodeMie extends OpenAI {

	public static String url = "https://keycloak.eks-core.aws.main.edp.projects.epam.com/auth/realms/codemie-prod/protocol/openid-connect/token";
	public static String baseUrl = "https://codemie.lab.epam.com/code-assistant-api/v1";

	@Override
	@CommandDescription("CodeMie processor supports command to call CodeMie API services.\r\n"
			+ "`username` and `password` are required.")
	@CommandExamples({ "<Extern class=\"CodeMie\" username=\"type:string\" password=\"type:password\"/>" })
	public void init(Processor parentProcessor, Node action) throws CommandException {
		String username = parentProcessor.attr(action, "username");
		String password = parentProcessor.attr(action, "password");

		try {
			String token = getToken(username, password);
			action.setAttribute("apiKey", token);
			action.setAttribute("baseUrl", baseUrl);
			super.init(parentProcessor, action);

		} catch (IOException e) {
			throw new CommandException("CodeMie processor inititialization faile: " + e.getMessage(), this);
		}
	}

	public static String getToken(String username, String password) throws IOException {
		String urlParameters = String.format("grant_type=password&client_id=codemie-sdk&username=%s&password=%s",
				username, password);

		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

		URL obj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setDoOutput(true);

		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
		}

		int responseCode = conn.getResponseCode();
		if (responseCode == 200) {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}

				String accessToken = StringUtils.substringBetween(response.toString(), "\"access_token\":\"", "\",");
				return accessToken;
			}
		} else {
			throw new IOException("Failed to obtain token: received HTTP response code " + responseCode);
		}
	}
}