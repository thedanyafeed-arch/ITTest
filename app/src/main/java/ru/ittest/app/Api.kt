package ru.ittest.app

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class User(
    val login: String, val password: String,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("middle_name") val middleName: String? = null,
    val role: Int? = 1
)
data class Profession(@SerializedName("prof_id") val id: Int, @SerializedName("prof_name") val name: String, @SerializedName("prof_description") val description: String? = null)
data class Question(@SerializedName("question_id") val id: Int, val question: String, @SerializedName("prof_id") val professionId: Int)
data class Answer(@SerializedName("answer_id") val id: Int, @SerializedName("question_id") val questionId: Int, val answer: String, val points: Int? = 0)
// Учебный API возвращает результат в единой оболочке: {"data": ..., "success": true}.
data class ApiResponse<T>(val data: T? = null, val success: Boolean = false)

interface ITTestApi {
    @GET("api/profile_users") fun login(@Query("login") login: String, @Query("password") password: String): Call<ApiResponse<List<User>>>
    @POST("api/profile_users") fun register(@Query("login") login: String, @Query("password") password: String, @Query("first_name") firstName: String, @Query("last_name") lastName: String = "", @Query("middle_name") middleName: String = ""): Call<ApiResponse<User>>
    @GET("api/professions") fun professions(): Call<ApiResponse<List<Profession>>>
    @GET("api/questions") fun allQuestions(): Call<ApiResponse<List<Question>>>
    @GET("api/questions/professions/{id}") fun questions(@Path("id") professionId: Int): Call<ApiResponse<List<Question>>>
    @GET("api/answers/{id}") fun answers(@Path("id") questionId: Int): Call<ApiResponse<List<Answer>>>
    @POST("api/results") fun saveResult(@Query("user_login") login: String, @Query("prof_id") professionId: Int, @Query("result_test") result: Double): Call<ApiResponse<Any>>
    @POST("api/questions") fun addQuestion(@Query("question") question: String, @Query("prof_id") professionId: Int): Call<ApiResponse<Question>>
    @POST("api/answers") fun addAnswer(@Query("question_id") questionId: Int, @Query("answer") answer: String, @Query("points") points: Int): Call<ApiResponse<Answer>>
}

object ServiceBuilder {
    // Учебный сервер указан в выданном задании.
    private const val BASE_URL = "http://172.30.44.151:8090/"
    private val client = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).build()
    val api: ITTestApi = Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(ITTestApi::class.java)
}
