package ch.hsr.baiot.openhab;

import android.test.AndroidTestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.hsr.baiot.openhab.model.ObjectAsArrayDeserializer;
import ch.hsr.baiot.openhab.model.Sitemap;
import ch.hsr.baiot.openhab.model.SitemapListHolder;
import ch.hsr.baiot.openhab.model.Widget;

/**
 * Created by dominik on 13.05.15.
 */
public class JsonDeserializerTest extends AndroidTestCase{

    public void testWidgetAcceptSingleValueAsArray() {
        String json = "{\"name\":\"demo\",\"label\":\"Main Menu\",\"link\":\"http://localhost:8080/rest/sitemaps/demo\",\"getHomepage\":{\"id\":\"demo\",\"title\":\"Main Menu\",\"link\":\"http://localhost:8080/rest/sitemaps/demo/demo\",\"leaf\":\"false\",\"widget\":{\"widgetId\":\"demo_0\",\"type\":\"Frame\",\"label\":\"\",\"icon\":\"frame\",\"widget\":{\"widgetId\":\"demo_0_0\",\"type\":\"Text\",\"label\":\"Test_Temp\",\"icon\":\"text\"}}}}";
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Widget[].class, new ObjectAsArrayDeserializer<Widget>(Widget.class))
                .create();

        Sitemap sitemap = gson.fromJson(json, Sitemap.class);
        assertNotNull(sitemap);
        assertEquals("demo", sitemap.name);
        assertNotNull(sitemap.homepage);
        assertEquals("demo", sitemap.homepage.id);
        assertNotNull(sitemap.homepage.widget);
        assertEquals(1, sitemap.homepage.widget.length);
        assertEquals("demo_0", sitemap.homepage.widget[0].widgetId);
        assertNotNull(sitemap.homepage.widget[0].widget);
        assertEquals(1, sitemap.homepage.widget[0].widget.length);
        assertEquals("demo_0_0", sitemap.homepage.widget[0].widget[0].widgetId);

    }

    public void testSitemapListHolderAcceptSingleValueAsArray() {
        String json = "{\"getSitemap\":{\"name\":\"demo\",\"label\":\"Main Menu\",\"link\":\"http://localhost:8080/rest/sitemaps/demo\",\"getHomepage\":{\"link\":\"http://localhost:8080/rest/sitemaps/demo/demo\",\"leaf\":\"false\"}}}";
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Sitemap[].class, new ObjectAsArrayDeserializer<Sitemap>(Sitemap.class))
                .create();

        SitemapListHolder holder = gson.fromJson(json, SitemapListHolder.class);
        assertNotNull(holder);
        assertNotNull(holder.sitemap);
        assertEquals(1, holder.sitemap.length);
        assertEquals("demo", holder.sitemap[0].name);
        assertNotNull(holder.sitemap[0].homepage);
    }
}
