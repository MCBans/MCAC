package uk.co.crashcraft.mcma.callback;

import uk.co.crashcraft.mcma.Main;

import uk.co.crashcraft.mcma.json.JSONException;
import uk.co.crashcraft.mcma.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JSONHandler {
	private Main MCAC;
	private boolean debug = true;
	public JSONHandler(Main p){
		MCAC = p;
	}
	public JSONObject get_data(String json_text){
	    try {
			JSONObject json = new JSONObject(json_text);
			return json;
		} catch (JSONException e) {
			if(debug){
				e.printStackTrace();
			}
		}
		return null;
	}
	public HashMap<String,String> mainRequest(HashMap<String,String> items){
		HashMap<String,String> out= new HashMap<String,String>();
		String url_req=this.urlparse(items);
		String json_text=this.request_from_api(url_req);
		JSONObject output=this.get_data(json_text);
		if(output!=null){

			Iterator<String> i = output.keys();
			if(i!=null){
				while(i.hasNext())
				{
				    String next = i.next();
				    try {
						out.put(next, output.getString(next));
					} catch (JSONException e) {
						if(debug){
							MCAC.logger.log("JSON Error On Retrieve");
							e.printStackTrace();
						}
					}
				}
			}
		}
		return out;
	}
	public JSONObject hdl_jobj(HashMap<String,String> items){
		String urlReq = urlparse(items);
		String jsonText = request_from_api(urlReq);
		JSONObject output = get_data(jsonText);
		return output;
	}
	public String request_from_api(String data){
		try {
			if(debug){
				MCAC.logger.log("Sending request!");
			}
			URL url = new URL("http://api.crashcraft.co.uk/" + MCAC.APIKey);
    	    URLConnection conn = url.openConnection();
    	    conn.setConnectTimeout(5000);
    	    conn.setReadTimeout(5000);
    	    conn.setDoOutput(true);
    	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    	    wr.write(data);
    	    wr.flush();
    	    StringBuilder buf = new StringBuilder();
    	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    	    String line;
    	    while ((line = rd.readLine()) != null) {
    	    	buf.append(line);
    	    }
    	    String result = buf.toString();
    	    if(debug){
    	    	MCAC.logger.log(result);
    	    }
    	    wr.close();
    	    rd.close();
			return result;
		} catch (Exception e) {
			if(debug){
				if(MCAC!=null){
					MCAC.logger.log("Fetch Data Error");
				}
				e.printStackTrace();
			}
			return "";
    	}
	}
	public String urlparse(HashMap<String,String> items){
		String data = "";
		try {
			for ( Map.Entry<String, String> entry : items.entrySet() ){
				String key = entry.getKey();
				String val = entry.getValue();
				if(data.equals("")){
					data = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
				}else{
					data += "&" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
				}
			}
		} catch (UnsupportedEncodingException e) {
			if(debug){
				e.printStackTrace();
			}
		}
		return data;
	}
}
