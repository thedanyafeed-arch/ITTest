package ru.ittest.app

import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import android.app.Activity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private var currentUser: User? = null
    private var selectedProfession: Profession? = null
    private var questions = emptyList<Question>()
    private var index = 0
    private var received = 0
    private var maximum = 0
    private val pad by lazy { (20 * resources.displayMetrics.density).roundToInt() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()

    override fun onCreate(state: Bundle?) { super.onCreate(state); showLogin() }

    private fun page(title: String, subtitle: String? = null) {
        val branded = title == "ITTest" || title == "Регистрация"
        window.statusBarColor = if (branded) Color.rgb(28, 63, 177) else Color.rgb(0, 105, 92)
        val frame = FrameLayout(this).apply { setBackgroundColor(if (branded) Color.rgb(65, 62, 180) else Color.rgb(247, 250, 249)) }
        if (branded) frame.addView(View(this).apply { setBackgroundColor(Color.rgb(27, 62, 177)) }, FrameLayout.LayoutParams(-1, dp(28), Gravity.TOP))
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(34), dp(28), dp(34), dp(28))
            if (branded) background = rounded(Color.WHITE, dp(18))
        }
        root.addView(TextView(this).apply {
            text = title; textSize = if (branded) 28f else 28f; setTextColor(Color.rgb(45, 45, 45)); setTypeface(null, 0)
            gravity = if (branded) Gravity.CENTER else Gravity.START; setPadding(0, 0, 0, dp(18))
        })
        subtitle?.let { root.addView(TextView(this).apply { text = it; textSize = 16f; setTextColor(Color.DKGRAY); setPadding(0, 0, 0, pad) }) }
        val scroll = ScrollView(this).apply { isFillViewport = false; addView(root) }
        val margins = if (branded) FrameLayout.LayoutParams(-1, -2).apply { setMargins(dp(10), dp(48), dp(10), dp(250)); gravity = Gravity.TOP } else FrameLayout.LayoutParams(-1, -1).apply { setMargins(0, 0, 0, 0) }
        frame.addView(scroll, margins)
        if (branded) frame.addView(ITTestLogoView(this), FrameLayout.LayoutParams(dp(260), dp(245), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL))
        setContentView(frame)
    }
    private fun field(hint: String, password: Boolean = false) = EditText(this).apply { this.hint = hint; textSize = 18f; setTextColor(Color.rgb(45,45,45)); inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) } }
    private fun button(text: String, action: () -> Unit) = Button(this).apply { this.text = text; setTextColor(Color.WHITE); textSize = 17f; background = rounded(Color.BLACK, dp(14)); layoutParams = LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(10) }; setOnClickListener { action() } }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }
    private fun message(text: String) { Toast.makeText(this, text, Toast.LENGTH_LONG).show() }
    private fun <T> Call<ApiResponse<T>>.run(ok: (T?) -> Unit) = enqueue(object: Callback<ApiResponse<T>> {
        override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
            // Для пустого списка учебный сервер возвращает HTTP 200 и success:false.
            // Это валидный ответ на GET, а не ошибка соединения.
            if (response.isSuccessful) ok(response.body()?.data) else message("Ошибка сервера: ${response.code()}")
        }
        override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) { message("Нет связи с учебным сервером. Проверьте Wi-Fi/VPN.") }
    })
    private fun <T> Call<ApiResponse<T>>.runWrite(successText: String, ok: () -> Unit = {}) = enqueue(object: Callback<ApiResponse<T>> {
        override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
            val body = response.body()
            if (response.isSuccessful && body?.success == true) { message(successText); ok() }
            else message("Не удалось выполнить действие: сервер вернул ${response.code()}")
        }
        override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) { message("Нет связи с учебным сервером. Проверьте Wi-Fi/VPN.") }
    })

    private fun showLogin() {
        page("ITTest", "Войдите, чтобы пройти профориентационный тест")
        val login = field("Логин"); val password = field("Пароль", true)
        root.addView(login); root.addView(password)
        root.addView(button("ОК") {
            if (login.text.isBlank() || password.text.isBlank()) return@button message("Заполните логин и пароль")
            ServiceBuilder.api.login(login.text.toString(), password.text.toString()).run { users ->
                val user = users?.firstOrNull()
                if (user == null) message("Неверный логин или пароль") else { currentUser = user; showProfessions() }
            }
        }); root.addView(button("РЕГИСТРАЦИЯ") { showRegister() })
        root.addView(TextView(this).apply { text = "Войдите в существующий аккаунт или зарегистрируйтесь."; setPadding(0,pad,0,0); textSize = 14f })
    }

    private fun showRegister() {
        page("Регистрация", "Создайте аккаунт для сохранения результатов")
        val name = field("Имя"); val login = field("Логин"); val password = field("Пароль", true); val repeat = field("Повтор пароля", true)
        listOf(name, login, password, repeat).forEach(root::addView)
        root.addView(button("ОК") {
            when { name.text.isBlank() || login.text.isBlank() || password.text.isBlank() -> message("Заполните обязательные поля"); password.text.toString() != repeat.text.toString() -> message("Пароли не совпадают")
                else -> ServiceBuilder.api.register(login.text.toString(), password.text.toString(), name.text.toString()).runWrite("Регистрация выполнена") { showLogin() }
            }
        }); root.addView(button("НАЗАД") { showLogin() })
    }

    private fun showProfessions() {
        page("Выберите профессию", "Нажмите на профессию, чтобы начать тест")
        ServiceBuilder.api.professions().run { list ->
            if (list.isNullOrEmpty()) { root.addView(TextView(this).apply { text = "Профессии пока не добавлены" }); return@run }
            list.forEach { profession -> root.addView(button(profession.name) { selectedProfession = profession; loadTest(profession) }) }
            if (currentUser?.role == 0 || currentUser?.login == "admin") root.addView(button("СТРАНИЦА АДМИНИСТРАТОРА") { showAdmin() })
            root.addView(button("ВЫЙТИ") { currentUser = null; showLogin() })
        }
    }

    private fun loadTest(profession: Profession) {
        page(profession.name, "Загрузка вопросов...")
        ServiceBuilder.api.questions(profession.id).run { list ->
            questions = list.orEmpty(); index = 0; received = 0; maximum = 0
            if (questions.isEmpty()) message("Для этой профессии пока нет вопросов") else showQuestion()
        }
    }

    private fun showQuestion() {
        val question = questions[index]
        page(selectedProfession?.name ?: "Тест", "Вопрос ${index + 1} из ${questions.size}")
        root.addView(TextView(this).apply { text = question.question; textSize = 22f; setPadding(0,0,0,pad) })
        ServiceBuilder.api.answers(question.id).run { answers ->
            val values = answers.orEmpty()
            if (values.isEmpty()) {
                // В учебной БД могут быть черновики вопросов без ответов. Они не должны
                // блокировать весь тест и искажать процент результата.
                questions = questions.filterNot { it.id == question.id }
                message("Вопрос без вариантов ответа пропущен")
                if (questions.isEmpty() || index >= questions.size) showResult() else showQuestion()
                return@run
            }
            maximum += values.maxOf { it.points ?: 0 }
            values.forEach { answer -> root.addView(button(answer.answer) { received += answer.points ?: 0; if (++index == questions.size) showResult() else showQuestion() }) }
        }
    }

    private fun showResult() {
        val percent = if (maximum == 0) 0.0 else received * 100.0 / maximum
        val verdict = when { percent < 50 -> "Профессия не подходит"; percent < 70 -> "Профессия близка к пользователю"; else -> "Профессия подходит пользователю" }
        page("Результат", selectedProfession?.name)
        root.addView(TextView(this).apply { text = "${"%.1f".format(percent)}%"; textSize = 52f; gravity = Gravity.CENTER; setTextColor(Color.rgb(0,137,123)); setPadding(0,pad,0,pad) })
        root.addView(TextView(this).apply { text = verdict + "\n\n" + (selectedProfession?.description ?: ""); textSize = 19f; gravity = Gravity.CENTER })
        currentUser?.let { ServiceBuilder.api.saveResult(it.login, selectedProfession!!.id, percent).runWrite("Результат сохранён") }
        root.addView(button("ПРОЙТИ ЕЩЁ РАЗ") { loadTest(selectedProfession!!) }); root.addView(button("К ВЫБОРУ ПРОФЕССИИ") { showProfessions() })
    }

    private fun showAdmin() {
        page("Страница администратора", "Добавление вопросов и вариантов ответов")
        val q = field("Вопрос"); val profId = field("ID профессии"); root.addView(q); root.addView(profId)
        root.addView(button("ДОБАВИТЬ ВОПРОС") {
            val id = profId.text.toString().toIntOrNull()
            if (q.text.isBlank() || id == null) message("Укажите вопрос и ID профессии")
            else ServiceBuilder.api.addQuestion(q.text.toString(), id).runWrite("Вопрос добавлен") { showAdmin() }
        })
        root.addView(section("Доступные профессии (ID - название)"))
        ServiceBuilder.api.professions().run { list ->
            root.addView(TextView(this).apply { text = list.orEmpty().joinToString("\n") { "${it.id} - ${it.name}" }; textSize = 15f })
        }
        root.addView(space())
        val a = field("Ответ"); val questionId = field("ID вопроса"); val points = field("Кол-во баллов"); root.addView(a); root.addView(questionId); root.addView(points)
        root.addView(button("ДОБАВИТЬ ОТВЕТ") {
            val id = questionId.text.toString().toIntOrNull(); val score = points.text.toString().toIntOrNull()
            if (a.text.isBlank() || id == null || score == null) message("Заполните все поля ответа")
            else ServiceBuilder.api.addAnswer(id, a.text.toString(), score).runWrite("Ответ добавлен") { showAdmin() }
        })
        root.addView(section("Существующие вопросы (ID - вопрос)"))
        ServiceBuilder.api.allQuestions().run { list ->
            root.addView(TextView(this).apply { text = list.orEmpty().joinToString("\n") { "${it.id} - ${it.question}" }; textSize = 15f })
        }
        root.addView(button("НАЗАД") { showProfessions() })
    }
    private fun space() = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, pad) }
    private fun section(text: String) = TextView(this).apply { this.text = text; textSize = 18f; setTypeface(null, 1); setPadding(0, pad, 0, 8) }
    override fun onBackPressed() { showLogin() }
}

/** Нижний знак из макета: шестерёнка, круг и лампочка с названием приложения. */
private class ITTestLogoView(context: android.content.Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f; val cy = height * .54f; val gear = width * .36f
        paint.color = Color.BLACK
        repeat(8) { index ->
            canvas.save(); canvas.rotate(index * 45f, cx, cy)
            canvas.drawRoundRect(RectF(cx - gear * .23f, cy - gear * 1.05f, cx + gear * .23f, cy - gear * .45f), gear * .13f, gear * .13f, paint)
            canvas.restore()
        }
        canvas.drawCircle(cx, cy, gear * .78f, paint)
        paint.color = Color.rgb(65, 62, 180); canvas.drawCircle(cx, cy, gear * .68f, paint)
        paint.color = Color.WHITE; paint.textAlign = Paint.Align.CENTER; paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textSize = gear * .42f; canvas.drawText("ITtest", cx, cy + gear * .16f, paint)
        paint.textSize = gear * .50f; canvas.drawText("?", cx + gear * .12f, cy - gear * .32f, paint)
        paint.strokeWidth = gear * .11f; paint.style = Paint.Style.STROKE; paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(cx - gear * .48f, cy + gear * .72f, cx - gear * .85f, cy + gear * 1.10f, paint)
        canvas.drawCircle(cx - gear * .40f, cy + gear * .65f, gear * .12f, paint); paint.style = Paint.Style.FILL
    }
}
