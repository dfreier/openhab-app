package ch.hsr.baiot.openhab.service.api;


import ch.hsr.baiot.openhab.model.Page;
import ch.hsr.baiot.openhab.model.Sitemap;
import ch.hsr.baiot.openhab.model.SitemapListHolder;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by dominik on 06.05.15.
 */
public interface OpenHabApi {
    @GET("/rest/sitemaps?type=json")
    Observable<SitemapListHolder> getAllSitemaps();

    @GET("/rest/sitemaps/{sitemap}?type=json")
    Observable<Sitemap> getSitemap(@Path("sitemap") String sitemap);

    @GET("/rest/sitemaps/{sitemap}/{page}?type=json")
    Observable<Page> getPage(@Path("sitemap") String sitemap, @Path("page") String page);


    @Headers({
            "X-Atmosphere-Transport: long-polling",
            "X-Atmosphere-tracking-id: 12345",
            "X-Atmosphere-Framework: 1.0",
            "Accept: application/json"
    })
    @GET("/rest/sitemaps/{sitemap}/{page}?type=json")
    Observable<Page> subscribePage(@Path("sitemap") String sitemap, @Path("page") String page);
}
