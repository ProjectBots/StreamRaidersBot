# StreamRaiderBot

IMORTANT:  
At the moment StreamRaiders is detecting the bot and "shadow bans" everyone who uses it.  
You know that you got hit if the window for searching captains is empty.  
These bans may be appealed (no guarantee) by sending a support e-mail to SR and following instructions if any.  
  
Because of this <b>it is NOT recommended to use the bot in its current state</b>.  
I will make an update if anything changes.  

----

Disclaimer: this program is not from Stream Raiders nor affiliated with them. Use at own Risk

A bot for automated farming in Stream Raiders

We now have a [Discord Server](https://discord.gg/u7e5nTRaZQ)!

Docker version over [here](https://github.com/dead-f00l/StreamRaidersBot-docker)

First Steps:  
click the "add a profile" under "Bot" to add a profile (this bot can handle a lot of accounts for auto farming).  
enter a profilename (dont need to be the account name).  
hit enter, a browser will open where you login to StreamRaiders.  
let the website load, then just close the browser.  
wait a few seconds until you see the profile.  
press the play button and watch how it farms for you.   
  
to prevent bans this bot will wait 100 - 720 sec (by default) before it checks the raids again.

the guide can be opened under "Bot" (the guide is not up to date at the moment)  



***


### How to use the bot in your own project
add the bot module to your project  

Mavem:  
```xml

  <dependencies>
    <dependency>
      <groupId>com.github.ProjectBots</groupId>
      <artifactId>StreamRaidersBot</artifactId>
      <version>{last commit id or release tag}</version>
    </dependency>
    <dependency>
      <groupId>StreamRaidersBot</groupId>
      <artifactId>Bot</artifactId>
      <version>1.0.0</version>
    </dependency>
  </dependencies>
  
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
```

Initialise the Manager  
```java
Manager.ini(BotListener botlis);
```

Load all profiles  
```java
Manager.loadAllNewProfiles();
```

Stop the bot  
```java
Manager.stop();
```



Example:  
```java
	public static void main(String[] args) {
		//	initialise Manager
		try {
			Manager.ini(new BotListener() {
				@Override
				public boolean configNotReadable() {
					return false;
				}
			});
		} catch (IniCanceledException e) {
			e.printStackTrace();
			return;
		}
		
		//	load all profiles
		Manager.loadAllNewProfiles();
		
		//	start all profiles
		for(String cid : Manager.getLoadedProfiles())
			for(int i=0; i<5; i++)
				Manager.setRunning(cid, i, true);
		
		//	wait for any key
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//	exit bot
		Manager.stop();
	}
```

