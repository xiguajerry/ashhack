// Decompiled with: CFR 0.152
// Class Version: 17
package dev.realme.ash.api.account.msa;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.util.UndashedUuid;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.realme.ash.Ash;
import dev.realme.ash.api.account.msa.callback.BrowserLoginCallback;
import dev.realme.ash.api.account.msa.exception.MSAAuthException;
import dev.realme.ash.api.account.msa.model.MinecraftProfile;
import dev.realme.ash.api.account.msa.model.OAuthResult;
import dev.realme.ash.api.account.msa.model.XboxLiveData;
import dev.realme.ash.api.account.msa.security.PKCEData;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.session.Session;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MSAAuthenticator {
   private static final Logger LOGGER = LogManager.getLogger("MSA-Authenticator");
   private static final CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).disableAuthCaching().disableCookieManagement().disableDefaultUserAgent().build();
   private static final String CLIENT_ID = "d1bbd256-3323-4ab7-940e-e8a952ebdb83";
   private static final int PORT = 6969;
   private static final String REAL_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0";
   private static final String OAUTH_AUTH_DESKTOP_URL = "https://login.live.com/oauth20_authorize.srf?client_id=000000004C12AE6F&redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=token&locale=en";
   private static final String OAUTH_AUTHORIZE_URL = "https://login.live.com/oauth20_authorize.srf?response_type=code&client_id=%s&redirect_uri=http://localhost:%s/login&code_challenge=%s&code_challenge_method=S256&scope=XboxLive.signin+offline_access&state=NOT_NEEDED&prompt=select_account";
   private static final String OAUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
   private static final String XBOX_LIVE_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
   private static final String XBOX_XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
   private static final String LOGIN_WITH_XBOX_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
   private static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
   private static final Pattern SFTT_TAG_PATTERN = Pattern.compile("value=\"(.+?)\"");
   private static final Pattern POST_URL_PATTERN = Pattern.compile("urlPost:'(.+?)'");
   private HttpServer localServer;
   private String loginStage = "";
   private boolean serverOpen;
   private PKCEData pkceData;

   public Session loginWithCredentials(String email, String password) throws MSAAuthException, IOException {
      OAuthResult result = this.getOAuth();
      if (result.getPostUrl() == null || result.getSfttTag() == null) {
         throw new MSAAuthException("Failed to retrieve SFTT tag & Post URL");
      }
      String token = this.getOAuthLoginData(result, email, password);
      return this.loginWithToken(token, false);
   }

   public void loginWithBrowser(BrowserLoginCallback callback) throws IOException, URISyntaxException, MSAAuthException {
      if (!this.serverOpen || this.localServer == null) {
         this.localServer = HttpServer.create();
         this.localServer.createContext("/login", ctx -> {
            this.setLoginStage("Parsing access token from response");
            Map<String, String> query = this.parseQueryString(ctx.getRequestURI().getQuery());
            if (query.containsKey("error")) {
               String errorDescription = query.get("error_description");
               if (errorDescription != null && !errorDescription.isEmpty()) {
                  LOGGER.error("Failed to get token from browser login: {}", errorDescription);
                  this.writeToWebpage("Failed to get token: " + errorDescription, ctx);
                  this.setLoginStage(errorDescription);
               }
            } else {
               String code = query.get("code");
               if (code != null) {
                  callback.callback(code);
                  this.writeToWebpage("Successfully got code. You may now close this window", ctx);
               } else {
                  this.writeToWebpage("Failed to get code. Please try again.", ctx);
               }
            }
            this.serverOpen = false;
            this.localServer.stop(0);
         });
      }
      this.pkceData = this.generateKeys();
      if (this.pkceData == null) {
         throw new MSAAuthException("Failed to generate PKCE keys");
      }
      String url = String.format(OAUTH_AUTHORIZE_URL, CLIENT_ID, 6969, this.pkceData.challenge());
      if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
         Desktop.getDesktop().browse(new URI(url));
         this.setLoginStage("Waiting user response...");
      } else {
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         clipboard.setContents(new StringSelection(url), null);
         LOGGER.warn("BROWSE action not supported on Desktop Environment, copied to clipboard instead.");
         this.setLoginStage("Link copied to clipboard!");
      }
      if (!this.serverOpen) {
         this.localServer.bind(new InetSocketAddress(6969), 1);
         this.localServer.start();
         this.serverOpen = true;
      }
   }

   public Session loginWithToken(String token, boolean browser) throws MSAAuthException, IOException {
      this.setLoginStage("Logging in with Xbox Live...");
      XboxLiveData data = this.authWithXboxLive(token, browser);
      this.requestTokenFromXboxLive(data);
      String accessToken = this.loginWithXboxLive(data);
      this.setLoginStage("Fetching MC profile...");
      MinecraftProfile profile = this.fetchMinecraftProfile(accessToken);
      this.pkceData = null;
      return new Session(profile.username(), UndashedUuid.fromStringLenient(profile.id()), accessToken, Optional.empty(), Optional.empty(), Session.AccountType.MSA);
   }

   public String getLoginToken(String oauthToken) throws MSAAuthException, IOException {
      HttpPost httpPost = new HttpPost(OAUTH_TOKEN_URL);
      httpPost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Origin", "http://localhost:6969/");
      httpPost.setEntity(new StringEntity(this.makeQueryString(new String[][]{{"client_id", CLIENT_ID}, {"code_verifier", this.pkceData.verifier()}, {"code", oauthToken}, {"grant_type", "authorization_code"}, {"redirect_uri", "http://localhost:6969/login"}}), ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), Charset.defaultCharset())));
      CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost);
      try {
         String content = EntityUtils.toString(response.getEntity());
         if (content == null || content.isEmpty()) {
            throw new MSAAuthException("Failed to get login token from MSA OAuth");
         }
         JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
         if (obj.has("error")) {
            throw new MSAAuthException(obj.get("error").getAsString() + ": " + obj.get("error_description").getAsString());
         }
         String string = obj.get("access_token").getAsString();
         if (response != null) {
            response.close();
         }
         return string;
      }
      catch (Throwable throwable) {
         try {
            if (response != null) {
               try {
                  response.close();
               }
               catch (Throwable throwable2) {
                  throwable.addSuppressed(throwable2);
               }
            }
            throw throwable;
         }
         catch (IOException e) {
            e.printStackTrace();
            throw new MSAAuthException("Failed to get login token");
         }
      }
   }

   private OAuthResult getOAuth() throws MSAAuthException, IOException {
      HttpGet httpGet = new HttpGet(OAUTH_AUTH_DESKTOP_URL);
      httpGet.setHeader("User-Agent", REAL_USER_AGENT);
      httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
      CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);
      try {
         String content = EntityUtils.toString(response.getEntity());
         OAuthResult result = new OAuthResult();
         Matcher matcher = SFTT_TAG_PATTERN.matcher(content);
         if (matcher.find()) {
            result.setSfttTag(matcher.group(1));
         }
         if ((matcher = POST_URL_PATTERN.matcher(content)).find()) {
            result.setPostUrl(matcher.group(1));
         }
         List<Header> cookies = Arrays.asList(response.getHeaders("Set-Cookie"));
         result.setCookie(cookies.stream().map(NameValuePair::getValue).collect(Collectors.joining(";")));
         OAuthResult oAuthResult = result;
         if (response != null) {
            response.close();
         }
         return oAuthResult;
      }
      catch (Throwable throwable) {
         try {
            if (response != null) {
               try {
                  response.close();
               }
               catch (Throwable throwable2) {
                  throwable.addSuppressed(throwable2);
               }
            }
            throw throwable;
         }
         catch (IOException e) {
            e.printStackTrace();
            throw new MSAAuthException("Failed to login with email & password.");
         }
      }
   }

   /*
    * Enabled aggressive block sorting
    * Enabled unnecessary exception pruning
    * Enabled aggressive exception aggregation
    */
   private String getOAuthLoginData(OAuthResult result, String email, String password) throws MSAAuthException {
      String contentTypeRaw = ContentType.APPLICATION_FORM_URLENCODED.getMimeType();
      HttpPost httpPost = new HttpPost(result.getPostUrl());
      httpPost.setHeader("Cookie", result.getCookie());
      httpPost.setHeader("Content-Type", contentTypeRaw);
      String encodedEmail = URLEncoder.encode(email);
      String encodedPassword = URLEncoder.encode(password);
      httpPost.setEntity(new StringEntity(this.makeQueryString(new String[][]{{"login", encodedEmail}, {"loginfmt", encodedEmail}, {"passwd", encodedPassword}, {"PPFT", result.getSfttTag()}}), ContentType.create(contentTypeRaw)));
      HttpClientContext ctx = HttpClientContext.create();
      try (CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost, ctx);){
         List redirectLocations = ctx.getRedirectLocations();
         if (redirectLocations == null) throw new MSAAuthException("Failed to get valid response from Microsoft");
         if (redirectLocations.isEmpty()) throw new MSAAuthException("Failed to get valid response from Microsoft");
         String query = ((URI)redirectLocations.get(redirectLocations.size() - 1)).toString().split("#")[1];
         for (String param : query.split("&")) {
            String[] parameter = param.split("=");
            if (!parameter[0].equals("access_token")) continue;
            String string = parameter[1];
            return string;
         }
         String content = EntityUtils.toString(response.getEntity());
         if (content == null) throw new MSAAuthException("Failed to get access token");
         if (content.isEmpty()) throw new MSAAuthException("Failed to get access token");
         if (content.contains("Sign in to")) {
            throw new MSAAuthException("The provided credentials were incorrect");
         }
         if (!content.contains("Help us protect your account")) throw new MSAAuthException("Failed to get access token");
         throw new MSAAuthException("2FA has been enabled on this account");
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      throw new MSAAuthException("Failed to get access token");
   }

   private XboxLiveData authWithXboxLive(String accessToken, boolean browser) throws MSAAuthException, IOException {
      String body = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"" + (browser ? "d=" : "") + accessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
      String content = this.makePostRequest(XBOX_LIVE_AUTH_URL, body, ContentType.APPLICATION_JSON);
      if (content != null && !content.isEmpty()) {
         JsonObject object = JsonParser.parseString(content).getAsJsonObject();
         XboxLiveData data = new XboxLiveData();
         data.setToken(object.get("Token").getAsString());
         data.setUserHash(object.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString());
         return data;
      }
      throw new MSAAuthException("Failed to authenticate with Xbox Live account");
   }

   private void requestTokenFromXboxLive(XboxLiveData xboxLiveData) throws MSAAuthException, IOException {
      String body = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xboxLiveData.getToken() + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
      String content = this.makePostRequest(XBOX_XSTS_AUTH_URL, body, ContentType.APPLICATION_JSON);
      if (content != null && !content.isEmpty()) {
         JsonObject object = JsonParser.parseString(content).getAsJsonObject();
         if (object.has("XErr")) {
            throw new MSAAuthException("Xbox Live Error: " + object.get("XErr").getAsString());
         }
         xboxLiveData.setToken(object.get("Token").getAsString());
      }
   }

   private String loginWithXboxLive(XboxLiveData data) throws MSAAuthException, IOException {
      String body = "{\"ensureLegacyEnabled\":true,\"identityToken\":\"XBL3.0 x=" + data.getUserHash() + ";" + data.getToken() + "\"}";
      String content = this.makePostRequest(LOGIN_WITH_XBOX_URL, body, ContentType.APPLICATION_JSON);
      if (content != null && !content.isEmpty()) {
         JsonObject object = JsonParser.parseString(content).getAsJsonObject();
         if (object.has("errorMessage")) {
            throw new MSAAuthException(object.get("errorMessage").getAsString());
         }
         if (object.has("access_token")) {
            return object.get("access_token").getAsString();
         }
      }
      return null;
   }

   private MinecraftProfile fetchMinecraftProfile(String accessToken) throws MSAAuthException, IOException {
      HttpGet httpGet = new HttpGet(MINECRAFT_PROFILE_URL);
      httpGet.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
      httpGet.setHeader("Authorization", "Bearer " + accessToken);
      CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);
      try {
         if (response.getStatusLine().getStatusCode() != 200) {
            throw new MSAAuthException("Failed to fetch MC profile: Status code != 200, sc=" + response.getStatusLine().getStatusCode());
         }
         String rawJSON = EntityUtils.toString(response.getEntity());
         JsonObject object = JsonParser.parseString(rawJSON).getAsJsonObject();
         if (object.has("error")) {
            throw new MSAAuthException("Failed to fetch MC profile: " + object.get("error").getAsString() + " -> " + object.get("errorMessage").getAsString());
         }
         MinecraftProfile minecraftProfile = new MinecraftProfile(object.get("name").getAsString(), object.get("id").getAsString());
         if (response != null) {
            response.close();
         }
         return minecraftProfile;
      }
      catch (Throwable throwable) {
         try {
            if (response != null) {
               try {
                  response.close();
               }
               catch (Throwable throwable2) {
                  throwable.addSuppressed(throwable2);
               }
            }
            throw throwable;
         }
         catch (IOException e) {
            throw new MSAAuthException(e.getMessage());
         }
      }
   }

   private String makePostRequest(String url, String body, ContentType contentType) throws IOException {
      HttpPost httpPost = new HttpPost(url);
      httpPost.setHeader("Content-Type", contentType.getMimeType());
      httpPost.setHeader("Accept", "application/json");
      httpPost.setEntity(new StringEntity(body, ContentType.create(contentType.getMimeType(), Charset.defaultCharset())));
      CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost);
      try {
         String string = EntityUtils.toString(response.getEntity());
         if (response != null) {
            response.close();
         }
         return string;
      }
      catch (Throwable throwable) {
         try {
            if (response != null) {
               try {
                  response.close();
               }
               catch (Throwable throwable2) {
                  throwable.addSuppressed(throwable2);
               }
            }
            throw throwable;
         }
         catch (IOException e) {
            Ash.error("Failed to make POST request to {}", url);
            e.printStackTrace();
            return null;
         }
      }
   }

   private void writeToWebpage(String message, HttpExchange ext) throws IOException {
      byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
      ext.sendResponseHeaders(200, message.length());
      OutputStream outputStream = ext.getResponseBody();
      outputStream.write(bytes, 0, bytes.length);
      outputStream.close();
   }

   private String makeQueryString(String[][] parameters) {
      StringJoiner joiner = new StringJoiner("&");
      for (String[] parameter : parameters) {
         joiner.add(parameter[0] + "=" + parameter[1]);
      }
      return joiner.toString();
   }

   private Map<String, String> parseQueryString(String query) {
      LinkedHashMap<String, String> parameterMap = new LinkedHashMap<String, String>();
      for (String part : query.split("&")) {
         String[] kv = part.split("=");
         parameterMap.put(kv[0], kv.length == 1 ? null : kv[1]);
      }
      return parameterMap;
   }

   private PKCEData generateKeys() {
      try {
         byte[] randomBytes = new byte[32];
         new SecureRandom().nextBytes(randomBytes);
         String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
         byte[] verifierBytes = verifier.getBytes(StandardCharsets.US_ASCII);
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         digest.update(verifierBytes, 0, verifierBytes.length);
         byte[] d = digest.digest();
         String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(d);
         return new PKCEData(challenge, verifier);
      }
      catch (Exception exception) {
         return null;
      }
   }

   public void setLoginStage(String loginStage) {
      this.loginStage = loginStage;
   }

   public String getLoginStage() {
      return this.loginStage;
   }
}
