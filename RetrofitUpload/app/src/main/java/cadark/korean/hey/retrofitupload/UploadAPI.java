package cadark.korean.hey.retrofitupload;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.POST;

/**
 * Created by Hey.Hung on 3/15/2016.
 */
public interface UploadAPI {
    @POST("/upload.php")
    public void insertImage(
            @Field("image") String image,
            @Field("name") String name,
            Callback<Response> callback);
}
