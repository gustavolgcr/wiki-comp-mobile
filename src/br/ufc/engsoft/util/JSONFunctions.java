package br.ufc.engsoft.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.UiThread;

import android.util.Log;

/*
 * Classe na qual vamos trabalhar com o manuseio com o
 * webservice. Aqui funcoes para trabalhar com o JSON
 * retornado pelo webservice da mediawiki
 */

public class JSONFunctions {

	// Metodo que sera executado em uma thread separada
	@Background
	public static String getJSONfromURL(String url) {
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

		// http post
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		} catch (Exception e) {
			Log.e("log_tag", "Error in http connection " + e.toString());
		}

		// convert response to string
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			Log.e("log_tag", "Error converting result " + e.toString());
		}

		try {

			jArray = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		Log.i("result", result);

		// Retirando a parte que tem o nome [editar]
		// result.replace("[editar]","");
		return result;
	}

	/*
	 * Seta o que queremos pesquisar.Por enquanto ainda esta case sensitive e
	 * com acentos.
	 */
	public static String setTextSearch(String title) {
		String title_encoded = URLEncoder.encode(title);
		String url = "http://wiki.dc.ufc.br/mediawiki/api.php?format=json&action=query&titles=";
		url = url + title_encoded;
		return url;
	}

	/*
	 * Dado um titulo de uma pagina da mediawiki, este metodo concatena este
	 * titulo encoded, isto e, se tiver algum caracter especial nao tera
	 * problemas agora.
	 */
	public static String setURLPost(String title) {
		String title_encoded = URLEncoder.encode(title);
		String url = "http://wiki.dc.ufc.br/mediawiki/api.php?action=parse&format=json&page=";
		url += title_encoded;
		return url;
	}

	/*
	 * Ao pesquisarmos o titulo da pagina, esse titulo vem com algumas sujeiras,
	 * entao este metodo retira essa sujeira e retorna o titulo da pagina limpo.
	 * Isso realmente foi preciso fazer.
	 */
	public static String validarTitulo(String tituloPaginaSujo) {
		String tituloPaginaLimpo;

		tituloPaginaLimpo = tituloPaginaSujo.replace("\"", " ");
		tituloPaginaLimpo = tituloPaginaLimpo.replace(",", " ");
		tituloPaginaLimpo = tituloPaginaLimpo.replace("ns", " ");

		return tituloPaginaLimpo;
	}

	/*
	 * Retorna o titulo da pagina que foi procurada. este metodo utiliza o
	 * metodo de retirar a sujeiras, dado um titulo que foi buscado. Esse metodo
	 * eh utilizado na parte da busca
	 */
	@Background
	public static String getPageTitle(String title) {
		String url = setTextSearch(title);
		String json = JSONFunctions.getJSONfromURL(url);
		String pages = "";
		JSONObject json1;

		try {
			json1 = new JSONObject(json);
			pages = (String) json1.getJSONObject("query")
					.getJSONObject("pages").toString();
			Log.i("pages", pages);
			// Se tiver um '-1' eh porque a pagina nao foi achada.
			if (pages.contains("-1") || pages.contains("The page you specified doesn't exis")) {
				Log.i("page not found", pages);
				return "404 - Pagina Nao encontrada";
			} else {
				String[] v = pages.split(":");
				Log.i("ult. elem. ", v[2]);
				Log.i("limpo:", validarTitulo(v[2]));
				return validarTitulo(v[2]);
			}
		} catch (Exception e) {

		}

		return "Houve um erro ao procurar a página";

	}

}
