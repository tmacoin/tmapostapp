TMA Post App is an Android application, which allows users to use functionality below to interact with the TMA Coin network.

TMA Post App functionality includes the following main and sub menus:

* File menu: allows users to import, export, and copy fles; in addition, they can view system logs and show peers 
* Tools menu: allows users to change thier password, get balances, and send Coins.
* Messaging: allows users to send secure encrypted messages between TMA addresses.
* Tmitter: smiliar to twitter but with a few modifications; explained below
* Posting announcements,  which can be used as decentralized, distributed, uncensored version of Yelp, Craiglist or eBay.

Tma Post App does not require downloading of blockchain and can be installed and started quickly; most importantly, TMA Post App uses less resources on your phone. 

<b>Installation instructions:</b>

1. Go to your phone's Download directory(either phone internal or SD ok). 

   ![download](app/images/android1.png)
   

2. Click on the  [APK file](tma.apk) in the tmapostapp directory and download it to the directory in step one above.

{% raw %}
<button onclick="window.open('/tma.apk')"APK file</button>
{% endraw %}

3. If you already have keys.csv file with existing keys, just copy it to the download folder, otherwise, it will create new key. 
4. Double click on the APK file located in the APK directory from step one above. 

   ![apkFile](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android2.png)

5. Click Install

   ![install](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android3.png)

6. Once installed, select Open. Important: if you have a Keys.csv file, copy it to the APK directory before you select Open.

<b>Logging in to App</b>

If you do not have an existing keys.csv file, it will prompt  you for a passphrase and confirm passphrase.

If you do have a passphrase, it will only prompt you for the existing passphrase. The example exhibit below is the prompt when a user  copied their existing keys.csv file to the Apk directory and then started the app.

  ![login](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android4.png)

It will then the start network by connecting to full peers specified in peers.config and locals.config in case you have full instances running on your local network. Once logged in, it will display your address.

To access the main menu, select the ellipsis or dot menu located on the upper right corner. This will display the main menu.

  ![mainMenu](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android5.png)



<b>File sub menu:</b> allows users to import, export, and copy fles; in addition, they can view system logs and show peers
* Stop Message Listener: if this app is stops responding, select this option
* Import File: select a file to import(see exhibit below)
* Export File: select a file to export(see exhibit below)
* View Log: use to identify any issues with app
* Shop Peers: this list peers connected to the same instance you are connected
    
![mainMenu](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android8.png)

 Import File:
 
 ![import](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android6.png)


 Export File:
 
 ![export](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android7.png)
 


<b>Tools sub menu:</b> allows users to change thier password, get balances, and send Coins.
* Change Password: allows user to change thier password
* Get Any Balance: allows user to enter any address and retrieve its balance
* Get My Address: allows user to get thier address 
* Send Coins: allows user to send coins
* Show Address: allows user to see their current address

![mainMenu](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android9.png)

 Change Password:
 
 ![password](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android10.png)
 
 Get Any Balance:
 
 ![anyBalance](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android11.png)
 
 Get My Balance:
 
 ![myBalance](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android12.png)
 
 
 Send Coins
* Recipient TMA Address: this is the address your are sending money 
* Amount in Coins: number of coins you are sending
* Fee in satoshis: the fee you are charging to send Coins
* Data: information related to transaction(never deleted)
* Expiring after # blocks: data to expire or deleted after number of bloacks created after the bloack to include this transaction
* Data: information which will expire

 ![sendCoins](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android13.png)

Show Address:

![showAddress](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android14.png)



<b>Secure Messaging sub menu:</b>
* Send Secure Messaging: allows user to send a Secure Encrypted message.
* Show messages: allows user to view a list of their messages

![messageMenu](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android15.png)

Send Secure(similiar to cellular text messaging):
* Recipient TMA Address: this is the address your are sending money 
* Fee in satoshis: the fee you are charging to send Coins
* Expiring after # blocks: data to expire or deleted after number of bloacks created after the bloack to include this transaction
* Subject: information which will expire
* Body: information which will expire after number blocks mined

![secureMessage](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android16.png)

Show Messages:

![showMessage](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android17.png)

<b>Tmitter sub menu:</b>
* Create Tmitter: allows user to create Tmitter account for current TMA Coin address
* Show my Tmeets: list number of Tmeets user sent            
* Send Tmeets: allows user to send Tmeets to user's subscribed to your account
* Search Tmeet: allows user to list Tmitter received for a specific Tmitter Account you subscribed (case sensitive)
* My Subscription: allows you to see latest Tmeets for the Tmeet accounts you have subscribed (note: there is no notification when you receive a Tmeet)

![tmeetsMenu](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android18.png)


Create Tmitter


Show my Tmeets:

![showTmeets](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android19.png)


Send Tmeets:

![sendTmeets](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android20.png)


Search Tmeets:

![searchTmeets](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android21.png)


My Subscription:

![tmeetsSubSript](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android22.png)


<b>Posts sub menu:</b>
* Create Post: allows user to create a Posting account on TMA Coin Network
* Find Post: allows user to find a Posting by entering a Posting Account and keywords        
* My Ratings: list your ratings of a particular Post on TMA Coin Network
* My Posts: list all your Posts submitted to TMA Coin Network

![postMenu](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android23.png)

Create your own posts or submit reviews of any other posts.

<B>NEED IMAGE</B>

Find Posts

![findPosts](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android24.png)

My Ratings

![findPosts](https://raw.githubusercontent.com/tmacoin/tmapost/master/tmapost/images/android25.png)

My Posts

<B>NEED IMAGE</B>






Posts and reviews are stored on TMA blockchain and can be only removed by the original poster. As any other blockchain it is censorship free.

Limited web version of TMA Post can be viewed for demonstration purpose at https://www.tmacoin.org/

