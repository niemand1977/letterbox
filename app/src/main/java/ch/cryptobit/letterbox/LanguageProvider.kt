package ch.cryptobit.letterbox

import java.util.Locale

/* One entry per supported app language. `code` is the BCP-47 language tag
 * used both to detect the system locale and to select a TextToSpeech voice. */
enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    GERMAN("de"),
    FRENCH("fr"),
    SPANISH("es"),
    ITALIAN("it"),
    KOREAN("ko"),
    JAPANESE("ja"),
    POLISH("pl"),
    PORTUGUESE_BR("pt-BR");

    val locale: Locale get() = Locale.forLanguageTag(code)

    companion object {
        /* Falls back to English if the device's language isn't one we support. */
        fun fromSystemLocale(): AppLanguage {
            val locale = Locale.getDefault()
            val language = locale.language
            val region = locale.country
            entries.find { it.code == "$language-$region" }?.let { return it }
            entries.find { it.code == language }?.let { return it }
            return ENGLISH
        }
    }
}

data class AnimalEntry(
    val drawable: Int,
    val translations: Map<AppLanguage, Pair<Char, String>>
)

object LanguageMaps {

    val allAnimals = listOf(
        AnimalEntry(R.drawable.ant, mapOf(
            AppLanguage.ENGLISH to ('A' to "Ant"),
            AppLanguage.GERMAN to ('A' to "Ameise"),
            AppLanguage.POLISH to ('A' to "Mrówka"),
            AppLanguage.JAPANESE to ('あ' to "あり"),
            AppLanguage.KOREAN to ('ㄱ' to "개미")
        )),
        AnimalEntry(R.drawable.bear, mapOf(
            AppLanguage.ENGLISH to ('B' to "Bear"),
            AppLanguage.GERMAN to ('B' to "Bär"),
            AppLanguage.FRENCH to ('O' to "Ours"),
            AppLanguage.SPANISH to ('O' to "Oso"),
            AppLanguage.ITALIAN to ('O' to "Orso"),
            AppLanguage.POLISH to ('N' to "Niedźwiedź"),
            AppLanguage.JAPANESE to ('く' to "くま"),
            AppLanguage.KOREAN to ('ㄱ' to "곰")
        )),
        AnimalEntry(R.drawable.beaver, mapOf(
            AppLanguage.ENGLISH to ('B' to "Beaver"),
            AppLanguage.GERMAN to ('B' to "Biber"),
            AppLanguage.POLISH to ('B' to "Bóbr"),
            AppLanguage.JAPANESE to ('び' to "びーばー"),
            AppLanguage.KOREAN to ('ㅂ' to "비버")
        )),
        AnimalEntry(R.drawable.bunny, mapOf(
            AppLanguage.GERMAN to ('H' to "Hase"),
            AppLanguage.KOREAN to ('ㅌ' to "토끼"),
            AppLanguage.JAPANESE to ('う' to "うさぎ")
        )),
        AnimalEntry(R.drawable.butterfly, mapOf(
            AppLanguage.ITALIAN to ('F' to "Farfalla"),
            AppLanguage.KOREAN to ('ㄴ' to "나비"),
            AppLanguage.JAPANESE to ('ち' to "ちょう")
        )),
        AnimalEntry(R.drawable.cat, mapOf(
            AppLanguage.ENGLISH to ('C' to "Cat"),
            AppLanguage.SPANISH to ('G' to "Gato"),
            AppLanguage.POLISH to ('C' to "Kot"),
            AppLanguage.GERMAN to ('K' to "Katze"),
            AppLanguage.JAPANESE to ('ね' to "ねこ"),
            AppLanguage.KOREAN to ('ㄱ' to "고양이")
        )),
        AnimalEntry(R.drawable.chameleon, mapOf(
            AppLanguage.ENGLISH to ('C' to "Chameleon"),
            AppLanguage.GERMAN to ('C' to "Chamäleon"),
            AppLanguage.FRENCH to ('C' to "Caméléon"),
            AppLanguage.SPANISH to ('C' to "Camaleón"),
            AppLanguage.ITALIAN to ('C' to "Camaleonte"),
            AppLanguage.PORTUGUESE_BR to ('C' to "Camaleão"),
            AppLanguage.JAPANESE to ('か' to "かめれおん"),
            AppLanguage.KOREAN to ('ㅋ' to "카멜레온")
        )),
        AnimalEntry(R.drawable.chimpanzee, mapOf(
            AppLanguage.ENGLISH to ('C' to "Chimpanzee"),
            AppLanguage.KOREAN to ('ㅊ' to "침팬지"),
            AppLanguage.JAPANESE to ('ち' to "ちんぱんじー")
        )),
        AnimalEntry(R.drawable.cow, mapOf(
            AppLanguage.ENGLISH to ('C' to "Cow"),
            AppLanguage.GERMAN to ('K' to "Kuh"),
            AppLanguage.SPANISH to ('V' to "Vaca"),
            AppLanguage.ITALIAN to ('M' to "Mucca"),
            AppLanguage.PORTUGUESE_BR to ('V' to "Vaca"),
            AppLanguage.JAPANESE to ('う' to "うし"),
            AppLanguage.KOREAN to ('ㅅ' to "소")
        )),
        AnimalEntry(R.drawable.deer, mapOf(
            AppLanguage.ENGLISH to ('D' to "Deer"),
            AppLanguage.FRENCH to ('B' to "Biche"),
            AppLanguage.ITALIAN to ('D' to "Daino"),
            AppLanguage.KOREAN to ('ㅅ' to "사슴"),
            AppLanguage.JAPANESE to ('し' to "しか")
        )),
        AnimalEntry(R.drawable.dinosaur, mapOf(
            AppLanguage.ENGLISH to ('D' to "Dinosaur"),
            AppLanguage.GERMAN to ('D' to "Dinosaurier"),
            AppLanguage.POLISH to ('D' to "Dinozaur"),
            AppLanguage.JAPANESE to ('き' to "きょうりゅう"),
            AppLanguage.KOREAN to ('ㄱ' to "공룡")
        )),
        AnimalEntry(R.drawable.dolphin, mapOf(
            AppLanguage.ENGLISH to ('D' to "Dolphin"),
            AppLanguage.FRENCH to ('D' to "Dauphin"),
            AppLanguage.SPANISH to ('D' to "Delfín"),
            AppLanguage.GERMAN to ('D' to "Delfin"),
            AppLanguage.JAPANESE to ('い' to "いるか"),
            AppLanguage.KOREAN to ('ㄷ' to "돌고래")
        )),
        AnimalEntry(R.drawable.dromedary, mapOf(
            AppLanguage.ENGLISH to ('D' to "Dromedary"),
            AppLanguage.KOREAN to ('ㄹ' to "락다"),
            AppLanguage.PORTUGUESE_BR to ('D' to "Dromedário"),
            AppLanguage.GERMAN to ('D' to "Dromedar"),
            AppLanguage.JAPANESE to ('ら' to "らくだ")
        )),
        AnimalEntry(R.drawable.ducky, mapOf(
            AppLanguage.ENGLISH to ('D' to "Duck"),
            AppLanguage.ITALIAN to ('A' to "Anatra"),
            AppLanguage.KOREAN to ('ㅇ' to "오리"),
            AppLanguage.JAPANESE to ('あ' to "あひる"),
            AppLanguage.GERMAN to ('E' to "Ente")
        )),
        AnimalEntry(R.drawable.eagle, mapOf(
            AppLanguage.ENGLISH to ('E' to "Eagle"),
            AppLanguage.FRENCH to ('A' to "Aigle"),
            AppLanguage.SPANISH to ('A' to "Águila"),
            AppLanguage.PORTUGUESE_BR to ('A' to "Águia"),
            AppLanguage.GERMAN to ('A' to "Adler"),
            AppLanguage.JAPANESE to ('わ' to "わし"),
            AppLanguage.KOREAN to ('ㅅ' to "수리")
        )),
        AnimalEntry(R.drawable.elephant, mapOf(
            AppLanguage.ENGLISH to ('E' to "Elephant"),
            AppLanguage.GERMAN to ('E' to "Elefant"),
            AppLanguage.FRENCH to ('E' to "Éléphant"),
            AppLanguage.SPANISH to ('E' to "Elefante"),
            AppLanguage.ITALIAN to ('E' to "Elefante"),
            AppLanguage.KOREAN to ('ㅋ' to "코끼리"),
            AppLanguage.PORTUGUESE_BR to ('E' to "Elefante"),
            AppLanguage.JAPANESE to ('ぞ' to "ぞう")
        )),
        AnimalEntry(R.drawable.fish, mapOf(
            AppLanguage.ENGLISH to ('F' to "Fish"),
            AppLanguage.GERMAN to ('F' to "Fisch"),
            AppLanguage.POLISH to ('R' to "Ryba"),
            AppLanguage.JAPANESE to ('さ' to "さかな"),
            AppLanguage.KOREAN to ('ㅁ' to "물고기")
        )),
        AnimalEntry(R.drawable.flamingo, mapOf(
            AppLanguage.ENGLISH to ('F' to "Flamingo"),
            AppLanguage.FRENCH to ('F' to "Flamant"),
            AppLanguage.POLISH to ('F' to "Flaming"),
            AppLanguage.GERMAN to ('F' to "Flamingo"),
            AppLanguage.JAPANESE to ('ふ' to "ふらみんご"),
            AppLanguage.KOREAN to ('ㅎ' to "홍학")
        )),
        AnimalEntry(R.drawable.fox, mapOf(
            AppLanguage.ENGLISH to ('F' to "Fox"),
            AppLanguage.GERMAN to ('F' to "Fuchs"),
            AppLanguage.FRENCH to ('R' to "Renard"),
            AppLanguage.ITALIAN to ('V' to "Volpe"),
            AppLanguage.PORTUGUESE_BR to ('R' to "Raposa"),
            AppLanguage.JAPANESE to ('き' to "きつね"),
            AppLanguage.SPANISH to ('Z' to "Zorro"),
            AppLanguage.KOREAN to ('ㅇ' to "여우")
        )),
        AnimalEntry(R.drawable.frog, mapOf(
            AppLanguage.ENGLISH to ('F' to "Frog"),
            AppLanguage.SPANISH to ('R' to "Rana"),
            AppLanguage.ITALIAN to ('R' to "Rana"),
            AppLanguage.PORTUGUESE_BR to ('S' to "Sapo"),
            AppLanguage.GERMAN to ('F' to "Frosch"),
            AppLanguage.JAPANESE to ('か' to "かえる"),
            AppLanguage.KOREAN to ('ㄱ' to "개구리")
        )),
        AnimalEntry(R.drawable.giraffe, mapOf(
            AppLanguage.ENGLISH to ('G' to "Giraffe"),
            AppLanguage.GERMAN to ('G' to "Giraffe"),
            AppLanguage.FRENCH to ('G' to "Girafe"),
            AppLanguage.ITALIAN to ('G' to "Giraffa"),
            AppLanguage.PORTUGUESE_BR to ('G' to "Girafa"),
            AppLanguage.JAPANESE to ('き' to "きりん"),
            AppLanguage.KOREAN to ('ㄱ' to "기린")
        )),
        AnimalEntry(R.drawable.goose, mapOf(
            AppLanguage.ENGLISH to ('G' to "Goose"),
            AppLanguage.POLISH to ('G' to "Gęś"),
            AppLanguage.GERMAN to ('G' to "Gans"),
            AppLanguage.JAPANESE to ('が' to "がちょう"),
            AppLanguage.KOREAN to ('ㄱ' to "거위")
        )),
        AnimalEntry(R.drawable.emu, mapOf(
            AppLanguage.POLISH to ('E' to "Emu"),
            AppLanguage.JAPANESE to ('え' to "えみゅー"),
            AppLanguage.KOREAN to ('ㅇ' to "에뮤")
        )),
        AnimalEntry(R.drawable.hamburger, mapOf(
            AppLanguage.ITALIAN to ('H' to "Hamburger"),
            AppLanguage.ENGLISH to ('H' to "Hamburger"),
            AppLanguage.GERMAN to ('H' to "Hamburger"),
            AppLanguage.JAPANESE to ('は' to "はんばーがー"),
            AppLanguage.KOREAN to ('ㅎ' to "햄버거")
        )),
        AnimalEntry(R.drawable.hippo, mapOf(
            AppLanguage.ENGLISH to ('H' to "Hippo"),
            AppLanguage.JAPANESE to ('か' to "かば"),
            AppLanguage.KOREAN to ('ㅎ' to "하마")
        )),
        AnimalEntry(R.drawable.hyena, mapOf(
            AppLanguage.ENGLISH to ('H' to "Hyena"),
            AppLanguage.POLISH to ('H' to "Hiena"),
            AppLanguage.GERMAN to ('H' to "Hyäne"),
            AppLanguage.JAPANESE to ('は' to "はいえな"),
            AppLanguage.KOREAN to ('ㅎ' to "하이에나")
        )),
        AnimalEntry(R.drawable.hedgehog, mapOf(
            AppLanguage.ENGLISH to ('H' to "Hedgehog"),
            AppLanguage.GERMAN to ('I' to "Igel"),
            AppLanguage.FRENCH to ('H' to "Hérisson"),
            AppLanguage.ITALIAN to ('I' to "Istrice"),
            AppLanguage.POLISH to ('J' to "Jeż"),
            AppLanguage.SPANISH to ('H' to "Erizo"),
            AppLanguage.PORTUGUESE_BR to ('H' to "Ouriço"),
            AppLanguage.JAPANESE to ('は' to "はりねずみ"),
            AppLanguage.KOREAN to ('ㄱ' to "고슴도치")
        )),
        AnimalEntry(R.drawable.iguana, mapOf(
            AppLanguage.ENGLISH to ('I' to "Iguana"),
            AppLanguage.FRENCH to ('I' to "Iguane"),
            AppLanguage.SPANISH to ('I' to "Iguana"),
            AppLanguage.ITALIAN to ('I' to "Iguana"),
            AppLanguage.PORTUGUESE_BR to ('I' to "Iguana"),
            AppLanguage.JAPANESE to ('い' to "いぐあな"),
            AppLanguage.POLISH to ('I' to "Iguana"),
            AppLanguage.KOREAN to ('ㅇ' to "이구아나")
        )),
        AnimalEntry(R.drawable.jaguar, mapOf(
            AppLanguage.ENGLISH to ('J' to "Jaguar"),
            AppLanguage.GERMAN to ('J' to "Jaguar"),
            AppLanguage.FRENCH to ('J' to "Jaguar"),
            AppLanguage.SPANISH to ('J' to "Jaguar"),
            AppLanguage.ITALIAN to ('J' to "Giaguaro"),
            AppLanguage.PORTUGUESE_BR to ('J' to "Jaguar"),
            AppLanguage.KOREAN to ('ㅈ' to "재규어"),
            AppLanguage.JAPANESE to ('じ' to "じゃがー")
        )),
        AnimalEntry(R.drawable.jellyfish, mapOf(
            AppLanguage.GERMAN to ('Q' to "Qualle"),
            AppLanguage.FRENCH to ('Q' to "Méduse"),
            AppLanguage.JAPANESE to ('く' to "くらげ"),
            AppLanguage.KOREAN to ('ㅎ' to "해파리")
        )),
        AnimalEntry(R.drawable.kangaroo, mapOf(
            AppLanguage.ENGLISH to ('K' to "Kangaroo"),
            AppLanguage.GERMAN to ('K' to "Känguru"),
            AppLanguage.FRENCH to ('K' to "Kangourou"),
            AppLanguage.POLISH to ('K' to "Kangur"),
            AppLanguage.JAPANESE to ('か' to "かんがるー"),
            AppLanguage.SPANISH to ('C' to "Canguro"),
            AppLanguage.KOREAN to ('ㅋ' to "캥거루")
        )),
        AnimalEntry(R.drawable.koala, mapOf(
            AppLanguage.ENGLISH to ('K' to "Koala"),
            AppLanguage.SPANISH to ('K' to "Koala"),
            AppLanguage.ITALIAN to ('K' to "Koala"),
            AppLanguage.PORTUGUESE_BR to ('K' to "Koala"),
            AppLanguage.JAPANESE to ('こ' to "こあら"),
            AppLanguage.GERMAN to ('K' to "Koala"),
            AppLanguage.KOREAN to ('ㅋ' to "코알라")
        )),
        AnimalEntry(R.drawable.lion, mapOf(
            AppLanguage.ENGLISH to ('L' to "Lion"),
            AppLanguage.GERMAN to ('L' to "Löwe"),
            AppLanguage.FRENCH to ('L' to "Lion"),
            AppLanguage.SPANISH to ('L' to "León"),
            AppLanguage.ITALIAN to ('L' to "Leone"),
            AppLanguage.PORTUGUESE_BR to ('L' to "Leão"),
            AppLanguage.JAPANESE to ('ら' to "らいおん"),
            AppLanguage.POLISH to ('L' to "Lew"),
            AppLanguage.KOREAN to ('ㅅ' to "사자")
        )),
        AnimalEntry(R.drawable.monkey, mapOf(
            AppLanguage.ENGLISH to ('M' to "Monkey"),
            AppLanguage.SPANISH to ('M' to "Mono"),
            AppLanguage.PORTUGUESE_BR to ('M' to "Macaco"),
            AppLanguage.JAPANESE to ('さ' to "さる"),
            AppLanguage.GERMAN to ('A' to "Affe"),
            AppLanguage.KOREAN to ('ㅇ' to "원숭이")
        )),
        AnimalEntry(R.drawable.mouse, mapOf(
            AppLanguage.ENGLISH to ('M' to "Mouse"),
            AppLanguage.GERMAN to ('M' to "Maus"),
            AppLanguage.POLISH to ('M' to "Mysz"),
            AppLanguage.JAPANESE to ('ね' to "ねずみ"),
            AppLanguage.FRENCH to ('S' to "Souris"),
            AppLanguage.KOREAN to ('ㅈ' to "쥐")
        )),
        AnimalEntry(R.drawable.nest, mapOf(
            AppLanguage.ENGLISH to ('N' to "Nest"),
            AppLanguage.GERMAN to ('N' to "Nest"),
            AppLanguage.FRENCH to ('N' to "Nid"),
            AppLanguage.SPANISH to ('N' to "Nido"),
            AppLanguage.ITALIAN to ('N' to "Nido"),
            AppLanguage.PORTUGUESE_BR to ('N' to "Ninho"),
            AppLanguage.JAPANESE to ('す' to "す"),
            AppLanguage.KOREAN to ('ㄷ' to "둥지")
        )),
        AnimalEntry(R.drawable.octopus, mapOf(
            AppLanguage.ENGLISH to ('O' to "Octopus"),
            AppLanguage.GERMAN to ('K' to "Krake"),
            AppLanguage.POLISH to ('O' to "Ośmiornica"),
            AppLanguage.KOREAN to ('ㅁ' to "문어"),
            AppLanguage.JAPANESE to ('た' to "たこ")
        )),
        AnimalEntry(R.drawable.penguin, mapOf(
            AppLanguage.ENGLISH to ('P' to "Penguin"),
            AppLanguage.GERMAN to ('P' to "Pinguin"),
            AppLanguage.FRENCH to ('P' to "Pingouin"),
            AppLanguage.SPANISH to ('P' to "Pingüino"),
            AppLanguage.ITALIAN to ('P' to "Pinguino"),
            AppLanguage.PORTUGUESE_BR to ('P' to "Pinguim"),
            AppLanguage.KOREAN to ('ㅍ' to "펭귄"),
            AppLanguage.POLISH to ('P' to "Pingwin"),
            AppLanguage.JAPANESE to ('ぺ' to "ぺんぎん")
        )),
        AnimalEntry(R.drawable.otter, mapOf(
            AppLanguage.GERMAN to ('O' to "Otter"),
            AppLanguage.JAPANESE to ('か' to "かわうそ"),
            AppLanguage.KOREAN to ('ㅅ' to "수달")
        )),
        AnimalEntry(R.drawable.square, mapOf(
            AppLanguage.POLISH to ('Q' to "Quadrat"),
            AppLanguage.GERMAN to ('Q' to "Quadrat"),
            AppLanguage.JAPANESE to ('し' to "しかく"),
            AppLanguage.KOREAN to ('ㅅ' to "사각형")
        )),
        AnimalEntry(R.drawable.picture, mapOf(
            AppLanguage.ITALIAN to ('Q' to "Quadro"),
            AppLanguage.JAPANESE to ('え' to "え"),
            AppLanguage.KOREAN to ('ㄱ' to "그림")
        )),
        AnimalEntry(R.drawable.four, mapOf(
            AppLanguage.FRENCH to ('Q' to "Quatre"),
            AppLanguage.JAPANESE to ('よ' to "よん"),
            AppLanguage.KOREAN to ('ㄴ' to "넷")
        )),
        AnimalEntry(R.drawable.quail, mapOf(
            AppLanguage.ENGLISH to ('Q' to "Quail"),
            AppLanguage.FRENCH to ('C' to "Caille"),
            // duetsch Wachtel ist nicht so bekannt
            AppLanguage.JAPANESE to ('う' to "うずら"),
            AppLanguage.KOREAN to ('ㅁ' to "메추라기")
        )),
        AnimalEntry(R.drawable.cheese, mapOf(
            AppLanguage.SPANISH to ('Q' to "Queso"),
            AppLanguage.PORTUGUESE_BR to ('Q' to "Queijo"),
            AppLanguage.JAPANESE to ('ち' to "ちーず"),
            AppLanguage.KOREAN to ('ㅊ' to "치즈")
        )),
        AnimalEntry(R.drawable.rainbow, mapOf(
            AppLanguage.ENGLISH to ('R' to "Rainbow"),
            AppLanguage.GERMAN to ('R' to "Regenbogen"),
            AppLanguage.JAPANESE to ('に' to "にじ"),
            AppLanguage.KOREAN to ('ㅁ' to "무지개")
        )),
        AnimalEntry(R.drawable.seal, mapOf(
            AppLanguage.ENGLISH to ('S' to "Seal"),
            AppLanguage.SPANISH to ('F' to "Foca"),
            AppLanguage.PORTUGUESE_BR to ('F' to "Foca"),
            AppLanguage.GERMAN to ('S' to "Seehund"),
            AppLanguage.JAPANESE to ('あ' to "あざらし"),
            AppLanguage.KOREAN to ('ㅁ' to "물개")
        )),
        AnimalEntry(R.drawable.sheep, mapOf(
            AppLanguage.ENGLISH to ('S' to "Sheep"),
            AppLanguage.FRENCH to ('M' to "Mouton"),
            AppLanguage.JAPANESE to ('ひ' to "ひつじ"),
            AppLanguage.PORTUGUESE_BR to ('O' to "Ovelha"),
            AppLanguage.GERMAN to ('S' to "Schaf"),
            AppLanguage.KOREAN to ('ㅇ' to "양")
        )),
        AnimalEntry(R.drawable.snake, mapOf(
            AppLanguage.ENGLISH to ('S' to "Snake"),
            AppLanguage.GERMAN to ('S' to "Schlange"),
            AppLanguage.FRENCH to ('S' to "Serpent"),
            AppLanguage.SPANISH to ('S' to "Serpiente"),
            AppLanguage.ITALIAN to ('S' to "Serpente"),
            AppLanguage.KOREAN to ('ㅂ' to "뱀"),
            AppLanguage.JAPANESE to ('へ' to "へび")
        )),
        AnimalEntry(R.drawable.tiger, mapOf(
            AppLanguage.ENGLISH to ('T' to "Tiger"),
            AppLanguage.GERMAN to ('T' to "Tiger"),
            AppLanguage.FRENCH to ('T' to "Tigre"),
            AppLanguage.SPANISH to ('T' to "Tigre"),
            AppLanguage.ITALIAN to ('T' to "Tigre"),
            AppLanguage.PORTUGUESE_BR to ('T' to "Tigre"),
            AppLanguage.KOREAN to ('ㅎ' to "호랑이"),
            AppLanguage.JAPANESE to ('と' to "とら"),
            AppLanguage.POLISH to ('T' to "Tygrys")
        )),
        AnimalEntry(R.drawable.eagle_owl, mapOf(
            AppLanguage.GERMAN to ('U' to "Uhu"),
            AppLanguage.POLISH to ('S' to "Sowa"),
            AppLanguage.JAPANESE to ('ふ' to "ふくろう"),
            AppLanguage.KOREAN to ('ㅅ' to "수리부엉이")
        )),
        AnimalEntry(R.drawable.unicorn, mapOf(
            AppLanguage.ENGLISH to ('U' to "Unicorn"),
            AppLanguage.FRENCH to ('U' to "Unicorne"),
            AppLanguage.SPANISH to ('U' to "Unicornio"),
            AppLanguage.ITALIAN to ('U' to "Unicorno"),
            AppLanguage.PORTUGUESE_BR to ('U' to "Unicórnio"),
            AppLanguage.POLISH to ('U' to "Jednorożec"),
            AppLanguage.JAPANESE to ('ゆ' to "ゆにこーん"),
            AppLanguage.KOREAN to ('ㅇ' to "유니콘")
        )),
        AnimalEntry(R.drawable.bird, mapOf(
            AppLanguage.GERMAN to ('V' to "Vogel"),
            AppLanguage.JAPANESE to ('と' to "とり"),
            AppLanguage.KOREAN to ('ㅅ' to "새")
        )),
        AnimalEntry(R.drawable.vulture, mapOf(
            AppLanguage.ENGLISH to ('V' to "Vulture"),
            AppLanguage.FRENCH to ('V' to "Vautour"),
            AppLanguage.KOREAN to ('ㄷ' to "독수리"),
            AppLanguage.JAPANESE to ('は' to "はげたか"),
            AppLanguage.GERMAN to ('G' to "Geier")
        )),
        AnimalEntry(R.drawable.volcano, mapOf(
            AppLanguage.POLISH to ('V' to "Vulkan"),
            AppLanguage.GERMAN to ('V' to "Vulkan"),
            AppLanguage.JAPANESE to ('か' to "かざん"),
            AppLanguage.KOREAN to ('ㅎ' to "화산")
        )),
        AnimalEntry(R.drawable.waffle, mapOf(
            AppLanguage.FRENCH to ('W' to "Waffle"),
            AppLanguage.ENGLISH to ('W' to "Waffle"),
            AppLanguage.JAPANESE to ('わ' to "わっふる"),
            AppLanguage.KOREAN to ('ㅇ' to "와플")
        )),
        AnimalEntry(R.drawable.whale, mapOf(
            AppLanguage.ENGLISH to ('W' to "Whale"),
            AppLanguage.GERMAN to ('W' to "Wal"),
            AppLanguage.SPANISH to ('B' to "Ballena"),
            AppLanguage.ITALIAN to ('B' to "Balena"),
            AppLanguage.PORTUGUESE_BR to ('B' to "Baleia"),
            AppLanguage.POLISH to ('W' to "Wieloryb"),
            AppLanguage.JAPANESE to ('く' to "くじら"),
            AppLanguage.KOREAN to ('ㄱ' to "고래")
        )),
        AnimalEntry(R.drawable.cup, mapOf(
            AppLanguage.PORTUGUESE_BR to ('X' to "Xícara"),
            // don't add cup or Tasse, we have better animals
            AppLanguage.JAPANESE to ('こ' to "こっぷ"),
            AppLanguage.KOREAN to ('ㅋ' to "컵")
        )),
        AnimalEntry(R.drawable.xerus, mapOf(
            AppLanguage.POLISH to ('X' to "Xerus"),
            AppLanguage.ENGLISH to ('X' to "Xerus"),
            // nicht so bekannt: GERMAN to ('E' to "Erdhörnchen"),
            AppLanguage.SPANISH to ('X' to "Xerus"),
            AppLanguage.PORTUGUESE_BR to ('X' to "Xerus"),
            AppLanguage.JAPANESE to ('り' to "りす"),
            AppLanguage.KOREAN to ('ㄸ' to "땅다람쥐")
        )),
        AnimalEntry(R.drawable.xylophone, mapOf(
            AppLanguage.ENGLISH to ('X' to "Xylophone"),
            AppLanguage.GERMAN to ('X' to "Xylophon"),
            AppLanguage.FRENCH to ('X' to "Xylophone"),
            AppLanguage.SPANISH to ('X' to "Xilófono"),
            AppLanguage.ITALIAN to ('X' to "Xilofono"),
            AppLanguage.JAPANESE to ('し' to "しろふぉん"),
            AppLanguage.KOREAN to ('ㅅ' to "실로폰")
        )),
        AnimalEntry(R.drawable.yak, mapOf(
            AppLanguage.ENGLISH to ('Y' to "Yak"),
            AppLanguage.GERMAN to ('Y' to "Yak"),
            AppLanguage.FRENCH to ('Y' to "Yak"),
            AppLanguage.SPANISH to ('Y' to "Yak"),
            AppLanguage.ITALIAN to ('Y' to "Yak"),
            AppLanguage.POLISH to ('Y' to "Jak"),
            AppLanguage.JAPANESE to ('や' to "やく"),
            AppLanguage.KOREAN to ('ㅇ' to "야크")
        )),
        AnimalEntry(R.drawable.yogurt, mapOf(
            AppLanguage.PORTUGUESE_BR to ('Y' to "Yogurte"),
            AppLanguage.JAPANESE to ('よ' to "よーぐると"),
            AppLanguage.KOREAN to ('ㅇ' to "요거트")
        )),
        AnimalEntry(R.drawable.zebra, mapOf(
            AppLanguage.ENGLISH to ('Z' to "Zebra"),
            AppLanguage.GERMAN to ('Z' to "Zebra"),
            AppLanguage.FRENCH to ('Z' to "Zèbre"),
            AppLanguage.ITALIAN to ('Z' to "Zebra"),
            AppLanguage.PORTUGUESE_BR to ('Z' to "Zebra"),
            AppLanguage.POLISH to ('Z' to "Zebra"),
            AppLanguage.JAPANESE to ('し' to "しまうま"),
            AppLanguage.KOREAN to ('ㅇ' to "얼룩말")
        ))
    )

    fun getMap(language: AppLanguage): Map<Char, List<Int>> {
        return allAnimals
            .filter { it.translations.containsKey(language) }
            .groupBy { it.translations[language]!!.first }
            .mapValues { (_, entries) -> entries.map { it.drawable } }
    }

    fun getAlphabet(language: AppLanguage): List<Char> {
        return allAnimals
            .filter { it.translations.containsKey(language) }
            .map { it.translations[language]!!.first }
            .distinct()
            .sorted()
    }

    /* The spoken word for this drawable in the given language, if any. */
    fun getWord(drawable: Int, language: AppLanguage): String? {
        return allAnimals
            .firstOrNull { it.drawable == drawable }
            ?.translations?.get(language)?.second
    }
}
