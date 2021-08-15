package xyz.defiancecoding.proxycheck.api.proxycheck.check;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.defiancecoding.proxycheck.api.webconnection.HTTPQuery;

import java.io.IOException;

public class ProxyCheck {

    private final ProxyCheckSettings settings = new ProxyCheckSettings();
    private final HTTPQuery httpQuery = new HTTPQuery();

    public ProxyCheck(){

    }

    private String urlBuilder(String ip){
        String baseURL = "http://proxycheck.io/v2/" + ip;
        baseURL += "?key=" + (settings.getApi_key());
        baseURL += "&vpn=" + (settings.isCheck_vpn() ? 1 : 0 );
        baseURL += "&asn=" + (settings.isCheck_asn() ? 1 : 0 );
        baseURL += "&node=" + (settings.isCheck_node() ? 1 : 0 );
        baseURL += "&time=" + (settings.isCheck_port() ? 1 : 0 );
        baseURL += "&risk=" + settings.setRiskLevel();
        baseURL += "&port=" + (settings.isCheck_port() ? 1 : 0);
        baseURL += "&seen=" + (settings.isCheck_seen() ? 1 : 0);
        baseURL += "&time=" + (settings.isCheck_time() ? 1 : 0);
        baseURL += "&days=" + settings.getMax_detection_days();
        if (settings.getVer() != null) {
            baseURL += "&ver=" + settings.getVer();
        }
        baseURL += "&tag=" + (settings.getTag());

        return baseURL;
    }


    //This is what you want to access for a basic String Json format
    public String getLookupResponse(String ip){
        return httpQuery.sendGet(urlBuilder(ip));
    }



    public JsonNode getRawJsonNode(String ip) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(getLookupResponse(ip));
    }


    public ProxyResults getAndMapResults(String ip) {

        ProxyResults result = new ProxyResults();
        try {

            JsonNode rawNode = getRawJsonNode(ip);

            result.setStatus(rawNode.get("status").asText());
            result.setNode(rawNode.get("node").asText());
            result.setQueryTime(rawNode.get("query time").asText());

            ObjectMapper mapper = new ObjectMapper();
            System.out.println("readTree: " + mapper.readTree(rawNode.get(ip).asText()));
            JsonNode subNode = mapper.readTree(String.valueOf(rawNode.get(ip)));

            System.out.println("RawNode: " + rawNode);
            System.out.println("subNode: " + subNode);


            result.setIp(ip);
            if (subNode.get("provider") != null) result.setProvider(subNode.get("provider").asText());
            if (subNode.get("continent") != null) result.setContinent(subNode.get("continent").asText());
            if (subNode.get("country") != null) result.setCountry(subNode.get("country").asText());
            if (subNode.get("city") != null) result.setCity(subNode.get("city").asText());
            if (subNode.get("region") != null) result.setRegion(subNode.get("region").asText());
            if (subNode.get("regioncode") != null) result.setRegionCode(subNode.get("regioncode").asText());
            if (subNode.get("latitude") != null) result.setLatitude(subNode.get("latitude").asText());
            if (subNode.get("longitude") != null) result.setLongitude(subNode.get("longitude").asText());
            if (subNode.get("isocode") != null) result.setIsoCode(subNode.get("isocode").asText());
            if (subNode.get("proxy") != null) result.setProxy(subNode.get("proxy").asText());
            if (subNode.get("type") != null) result.setType(subNode.get("type").asText());
            if (subNode.get("port") != null) result.setPort(subNode.get("port").asText("0000"));
            if (subNode.get("risk") != null) result.setRisk(subNode.get("risk").asText());
            if (subNode.get("last seen human") != null) result.setLastSeenHuman(subNode.get("last seen human").asText());
            if (subNode.get("last seen unix") != null) result.setLastSeenUnix(subNode.get("last seen unix").asText());

            JsonNode attackHistory = subNode.get("attack history");
            if (attackHistory != null) {
                result.setTotal(attackHistory.get("Total").asText());
                result.setVulnerabilityProbing(attackHistory.get("Vulnerability Probing").asText());
                result.setForumSpam(attackHistory.get("Forum Spam").asText());
                result.setRegistrationAttempt(attackHistory.get("Registration Attempt").asText());
                result.setLoginAttempt(attackHistory.get("Login Attempt").asText());
            }
            System.out.println("Result: " + result);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }



}
