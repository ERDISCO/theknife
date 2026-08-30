Installazione di Java
Per Windows: 
1.	Visitare il sito ufficiale Oracle Java: https://www.oracle.com/java/technologies/downloads/ 
2.	Scaricare la versione più recente di Java SE Development Kit (JDK) stabile
3.	Eseguire il file scaricato e seguire le istruzioni di installazione 
4.	Verificare l'installazione aprendo il Prompt dei comandi e digitando: java -version 
Per macOS: 
1.	Visitare il sito ufficiale Oracle Java: https://www.oracle.com/java/technologies/downloads/ 
2.	Scaricare la versione più recente di Java SE Development Kit (JDK) stabile
3.	Eseguire il file scaricato e seguire le istruzioni di installazione 
4.	Verificare l'installazione aprendo il Prompt dei comandi e digitando: java -version 
Per Linux: 
1.	Aggiorna l’indice dei pacchetti sudo apt update 
2.	Installa il pacchetto desiderato usando il commando “sudo apt install openjdk-21-jre”
3.	Verificare l’installazione java -version

Installazione di PostgreSQL
Per Windows e macOS: 
1.	Scaricare l’installer dal sito ufficiale, scegliendo l’ultima versione stabile in base al sistema operativo a disposizione: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads 
2.	Una volta scaricato, eseguire il file eseguibile e seguire le istruzioni di installazione 
Per Linux:
1.	Aggiorna l’indice dei pacchetti sudo apt update 
2.	Installa il pacchetto desiderato usando il commando “sudo apt install postgresql postgresql-contrib”

Durante l’installazione: una volta arrivati alla schermata in cui è richiesto il nome utente e la password, per non avere complicazioni con il server inserire ‘postgres’ come nome utente e ‘kebab’ come password

Installazione di Maven
Il setup vale per tutti e tre i sistemi operativi: 
1.	Scaricare l’installer dal sito ufficiale, scegliendo l’ultima versione stabile in base al sistema operativo a disposizione: https://maven.apache.org/download.cgi
2.	Una volta scaricato, decomprimere la cartella con maven in ‘Program Files’ nel PC se si è su windows.
3.	Verificare l'installazione aprendo il Prompt dei comandi e digitando: mvn -version

Download del programma
Il setup vale per tutti e tre i sistemi operativi: 
1.	Accedere al repository GitHub: Visitare il link del repository fornito https://github.com/ERDISCO/theknife.git
2.	Scaricare lo zip: Cliccare sul pulsante "Code" → "Download ZIP" 
3.	Estrarre l'archivio: Decomprimere il file ZIP scaricato in una cartella chiamata ‘theknife’ 
La cartella deve contenere questi file:
  theknife/
  ├── autori.txt              Cognome, nome, matricola e sede di ogni membro
  ├── pom.xml                 File di build Maven
  ├── README.txt              Questo file
  │
  ├── bin/                    Generata da mvn package
  │   ├── theknife-client-1.0.jar   Fat JAR del client
  │   ├── theknife-server-1.0.jar   Fat JAR del server
  │   ├── avvia-server.bat          Avvio server Windows (generato da Maven)
  │   ├── avvia-client.bat          Avvio client Windows (generato da Maven)
  │   ├── avvia-server.sh           Avvio server Linux/macOS (generato da Maven)
  │   └── avvia-client.sh           Avvio client Linux/macOS (generato da Maven)
  │
  ├── doc/                    Documentazione
  │   ├── javadoc/            Generata automaticamente da mvn package
  │   ├── theknife_db.sql     Dump del database (formato custom PostgreSQL)
  │   ├── manuale_utente.pdf
  │   ├── manuale_tecnico.pdf
  │   └── (diagrammi ER, UML...)
  │
  ├── lib/                    Librerie JAR di riferimento
  │   ├── bcrypt-0.10.2.jar
  │   ├── bytes-1.5.0.jar
  │   ├── checker-qual-3.42.0.jar
  │   ├── gson-2.10.1.jar
  │   └── postgresql-42.7.4.jar
  │
  └── src/                    Codice sorgente Java
      ├── client/
      ├── common/
      ├── gui/
      └── server/

Per Windows:
Una volta controllata la cartella, andare nelle variabili d’ambiente del sistema, cercando su Start “Modifica le variabili di ambiente relative al sistema”. Una volta qui selezionare “Variabili d’ambiente” situato in basso e nelle variabili di sistema selezionare la variabile ‘Path’ (oppure ’PATH’) e selezionare modifica. 
Se non si hanno già i percorsi, selezionare ‘Nuovo’, ed aggiungere i percorsi relativi alla cartella bin di ‘PostgreSQL’ e ‘apache-maven’ situati in ‘Program Files’.
Una volta finito selezionare ‘OK’ in tutte le finestre e procedere con il setup del database.

Setup database
Nel terminale, andare nella cartella theknife scaricata in precedenza e digitare i seguenti comandi: 
•	mvn exec:exec@create-db
•	mvn package
Nel caso ci siano errori, assicurarsi di aver selezionato i percorsi giusti nelle variabili di sistema e che le versioni dei software utilizzati siano le più recenti e stabili.

Esecuzione ed uso
Setup e lancio del programma

Avvio da interfaccia grafica (metodo più semplice)
1.	Navigare nella cartella bin dentro theknife
2.	Fare doppio clic sul file .bat (per windows) oppure .sh (per Linux e macOS)
3.	Il programma si avvierà in una finestra del terminale

Avvio da linea di comando
1. Aprire il terminale/prompt dei comandi nella cartella theknife
2. Digitare cd bin
3. Dopodiche digitare java -jar theknife-server-1.0.jar per avviare il server 
4. Nella finestra del pannello di controllo del server, avviare il server tramite l’apposito pulsante, ‘Avvia Server’ 
5. Digitare java -jar theknife-client-1.0.jar per avviare il client

Note importanti
•	Assicurarsi di aver installato Java SE Development Kit (JDK) versione 21 o superiore, PostgreSQL versione 18 o superiore e apache-maven versione 3.9.16 o superiore
•	Mantenere tutti i file nella stessa cartella per garantire il corretto funzionamento
•	Non modificare la struttura delle cartelle dopo l'installazione
• Non chiudere le finestre dei terminali mentre è in esecuzione il programma
