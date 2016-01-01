# journey-neo4j-plugin


Dev Env Setup
-------------
```
> brew install maven
> brew tap wpc/old-neo4j-versions
> brew install neo4j-2.2.3
> ./local_deploy
```

Dev tricks:
-----------
Tunnel to get access to neo4j console:
```
> ssh -v -i ~/.ssh/your-key ec2-user@[NEO-SERVER] -L 7475:localhost:7474 -N
```
