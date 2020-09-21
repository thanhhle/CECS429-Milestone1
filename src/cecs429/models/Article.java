package cecs429.models;

public class Article {
	public String title;
	public String body;
	public String url;
	
	/**
	 * Constructs an article with with given title, body, and url
	 * @param title subject of the article
	 * @param body content of the article
	 * @param url link of the article
	 */
	public Article(String title, String body, String url)
	{
		this.title = title;
		this.body = body;
		this.url = url;
	}

	/**
	 * Return the title of the article
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Return content of the article
	 * @return body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Return the link of the article
	 * @return url
	 */
	public String getUrl() {
		return url;
	}
}
