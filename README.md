# BarberShop Simulation

Feladat:
- Szimuláljunk egy borbélyüzletet, ahol egy borbély dolgozik napi 8 órát, majd bezár és másnap újból kinyit.
- Készüljön statisztika következőkről:
    - Hány vendég lett kiszolgálva
    - Hány vendég NEM lett kiszolgálva (indokkal: zárva volt/megtelt az üzlet)
    - Naponta mennyi embert szolgált ki a borbély
    - Átlagos várakozási idő

Szabályok:
- Vevők bármikor érkezhetnek. Ha a boltot zárva találják, akkor természetesen nem lesznek kiszolgálva, sorban állni nem tudnak a nyitásig.
- Az üzlet picike, így várakozni csak 5 széken lehet, a bolt előtt nem állhat sor.
- A kiszolgálás érkezési sorrendben történik, ha egy vevő kész, akkor következhet a sorban a következő. Ha nincs következő, a borbély addig pihen, míg nem érkezik újabb ügyfél, vagy le nem jár a nyitvatartási idő.
- A szimuláció egy órája legyen 400 ezredmásodperc. A kódot úgy kell megírni, hogy ez igény szerint módosítható legyen egyetlen ponton a kódban.
- Hajvágás ideje: véletlenszerűen 20 és 200 msec között
- Munkaidő (8 óra) - 3200msec
- 1 nap - 9600msec (0msec egy adott nap kezdete, 9600msec egy adott nap vége)
- Nyitvatartási idő: 
    - reggel 9-től délután 5-ig tart
    - fodrász kiszolgálja azokat a vevőket, akik záróra előtt érkeztek meg
    - A szimuláció egy munkahétig (5 nap) tartson, az első nap hajnali 00:00-tól az utolsó nap végéig, és a leállás előtt írja ki a statisztikákat.

    
