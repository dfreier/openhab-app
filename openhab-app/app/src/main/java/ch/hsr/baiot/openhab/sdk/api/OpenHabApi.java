package ch.hsr.baiot.openhab.sdk.api;



import ch.hsr.baiot.openhab.sdk.model.Page;
import ch.hsr.baiot.openhab.sdk.model.Sitemap;
import ch.hsr.baiot.openhab.sdk.model.SitemapListHolder;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.mime.TypedInput;
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

    @Headers("Content-type: text/plain")
    @POST("/rest/items/{item}")
    void sendCommand(@Path("item") String item, @Body TypedInput command, Callback<Void> callback);

}
