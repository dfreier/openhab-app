package ch.hsr.baiot.openhab.sdk.model;

/**
 * Created by dominik on 06.05.15.
 */
public class Sitemap {
    public String name;
    public String label;
    public String link;
    public Page homepage;

    public Sitemap() {}
    public Sitemap(String name, String label) {
        this.name = name;
        this.label = label;
    }
}
