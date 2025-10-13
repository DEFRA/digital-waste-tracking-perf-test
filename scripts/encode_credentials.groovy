import java.util.Base64

String clientId = props.get("clientId")
String clientSecret = props.get("clientSecret")
String credentials = "${clientId}:${clientSecret}"
String encoded = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"))
vars.put("basic_auth_header", "Basic " + encoded)
