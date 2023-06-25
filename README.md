### Technical Debt:
When faced with the error <br/> 
`Unable to instantiate class [org.bouncycastle.jce.provider.BouncyCastleProvider]
io.jsonwebtoken.lang.InstantiationException: Unable to instantiate class [org.bouncycastle.jce.provider.BouncyCastleProvider]` 
<br/> 

* Navigate to the 
Project Structure -> Libraries
and delete all dependencies with tag _org.bouncycastle_

* Run ``` mvn clean``` then ```mvn dependency:purge-local-repository```


