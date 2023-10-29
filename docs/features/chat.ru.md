# Особенности чата

## Шаблоны

Шаблоны - это система замены любых пользовательских слов/шаблонов.\
Каждое поле `chat.patterns` (config.yml) представляет собой новый шаблон, в котором указывается выражение, подлежащее
замене, и выражение, которым оно будет заменено.\
Выражения разделяются сочетанием символов "`,`".

Несколько стандартных шаблонов:

```yaml
chat:
  patterns:
    - ":) , ☺"
    - ":D , ☻"
    - ":( , ☹"
    - ":ok: , 🖒"
    - ":+1: , 🖒"
    - ":-1: , 🖓"
    - ":cool: , 😎"
    - "B) , 😎"
    - ":clown: , 🤡"
    - "<3 , ❤"
    - "xd , 😆"
    - "%) , 😵"
    - "=D , 😃"
    - ">:( , 😡"
    - ":idk: , ¯\\_(ツ)_/¯"
    - ":angry: , (╯°□°)╯︵ ┻━┻"
    - ":happy: , ＼(＾O＾)／"
```

## Предметы в чате

Во FlectoneChat реализована возможность отображения предметов в чате.\
Для этого достаточно написать в сообщении `%item%` и держать нужный предмет в руке.

![](https://i.imgur.com/m26PIre.png)

Вы также можете отобразить предмет, нажав по слоту шлема с зажатой кнопкой `shift`.

![](https://i.imgur.com/xN6yvtf.png)

## Скрытие слов (спойлеры)

Ваши игроки могут скрывать слова с помощью синтаксиса `||слово||`. Слово будет показано при наведении курсора

![](https://i.imgur.com/2z5nZ6A.gif)

## Упоминания (пинги)

Ваши игроки могут `@упоминать` других игроков в чате.\
Упоминания кликабельны и воспроизводят пользовательский звук для упомянутого игрока!