# Directions.

Hey there, if you need example for java advanced course you can read my solutions.
I just have to warn you that everyone can access them, so cheat at your own risk.

I have included the bash script which I tested all works with; it works on windows too if executed from GitBash. It works with a specific structure that you can change at your will (by changing the code).
You may see the example structure in this same repository, but basically folders "shared" and "old_tests" should be at the same level as the script and "info" folder.
For current course, "shared" folder should be used, there all .jar files should be added (everything inside "artifacts" and "lib")
The same for "old_test" folder if you wanna test with last year's testing files. (to do that you just have to add "o" as last argument to testing script)




# Everything else

Now, some words from our dear Георгий Александрович

Этот репозиторий склонирован для каждого студента и доступен по адресу
`https://www.kgeorgiy.info/git-students/year2021/<USER>/java-advanced`,
где `<USER>` — имя пользователя в PCMS (пароль так же используется из PCMS).

Для сдачи домашних заданий
 * Клонируйте ваш личный репозиторий
    * `git clone https://www.kgeorgiy.info/git-students/year2021/<USER>/java-advanced`
    * У личных репозиториев __нет__ web-интерфейса, используйте инструменты командной строки.
 * Добавьте ссылку на исходный репозиторий
    * `git remote add source https://www.kgeorgiy.info/git/geo/java-advanced-2023-solutions`
    * По мере появления новых домашних заданий в исходном репозитории будут появляться заготовки решений
      забирайте из через `git pull source master`.
 * Переименуйте пакет `info.kgeorgiy.ja.__last_name__`, заменив
   `__last_name__` на вашу фамилию.
    * В остальном сохраняйте текущую структуру каталогов и имена файлов.
    * Если структура репозитория не соответсвует исходной, преподаватель не будет проверять решение.
 * Добавляйте _только_ исходные файлы решений
    * Сохраняйте текущую структуру каталогов и имена файлов.
    * Не добавляйте исходный код тестов, `.class`-файлы и файлы проектов.
    * Не добавляйте тесты и другой код из репозитория с тестами.
    * Если структура репозитория не соответствует исходной, преподаватель не будет проверять решение.
    * Вы можете редактировать `.gitignore` как вам удобно.
 * Перед отправкой решения на проверку
    * Проверьте, что _все_ исходники компилируются (в том числе, тех ДЗ, которые вы не сдаёте).
    * Проверьте, что тесты сдаваемого ДЗ проходят.
    * Закоммитьте все изменения в `master`.
    * Запуште все изменения.
    * Запросите проверку решения, заполнив форму.
 * После проверки преподаватель либо укажет найденные недостатки в `NOTES.md`,
   либо укажет их в виде комментариев в исходном коде, пометив их как `:NOTE:`.
