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
	private final Main MCAC;
	private final boolean debug = true;
	public JSONHandler(Main p){
		this.MCAC = p;
	}
	public JSONObject get_data(String json_text){
	    try {
			final JSONObject json = new JSONObject(json_text);
			return json;
		} catch (final JSONException e) {
			if(this.debug){
				e.printStackTrace();
			}
		}
		return null;
	}
	public HashMap<String,String> mainRequest(HashMap<String,String> items){
		final HashMap<String,String> out= new HashMap<String,String>();
		final String url_req=this.urlparse(items);
		final String json_text=this.request_from_api(url_req);
		final JSONObject output=this.get_data(json_text);
		if(output!=null){

			final Iterator<String> i = output.keys();
			if(i!=null){
				while(i.hasNext())
				{
				    final String next = i.next();
				    try {
						out.put(next, output.getString(next));
					} catch (final JSONException e) {
						if(this.debug){
							this.MCAC.logger.log("JSON Error On Retrieve");
							e.printStackTrace();
						}
					}
				}
			}
		}
		return out;
	}
	public JSONObject hdl_jobj(HashMap<String,String> items){
		final String urlReq = this.urlparse(items);
		final String jsonText = this.request_from_api(urlReq);
		final JSONObject output = this.get_data(jsonText);
		return output;
	}
	public String request_from_api(String data){
		try {
			if(this.debug){
				this.MCAC.logger.log("Sending request!");
			}
			final URL url = new URL("http://api.crashcraft.co.uk/" + this.MCAC.APIKey);
    	    final URLConnection conn = url.openConnection();
    	    conn.setConnectTimeout(5000);
    	    conn.setReadTimeout(5000);
    	    conn.setDoOutput(true);
    	    final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    	    wr.write(data);
    	    wr.flush();
    	    final StringBuilder buf = new StringBuilder();
    	    final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    	    String line;
    	    while ((line = rd.readLine()) != null) {
    	    	buf.append(line);
    	    }
    	    final String result = buf.toString();
    	    if(this.debug){
    	    	this.MCAC.logger.log(result);
    	    }
    	    wr.close();
    	    rd.close();
			return result;
		} catch (final Exception e) {
			if(this.debug){
				if(this.MCAC!=null){
					this.MCAC.logger.log("Fetch Data Error");
				}
				e.printStackTrace();
			}
			return "";
    	}
	}
	public String urlparse(HashMap<String,String> items){
		String data = "";
		try {
			for ( final Map.Entry<String, String> entry : items.entrySet() ){
				final String key = entry.getKey();
				final String val = entry.getValue();
				if(data.equals("")){
					data = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
				}else{
					data += "&" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
				}
			}
		} catch (final UnsupportedEncodingException e) {
			if(this.debug){
				e.printStackTrace();
			}
		}
		return data;
	}
}
