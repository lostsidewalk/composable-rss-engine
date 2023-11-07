<link rel="stylesheet" type="text/css" href="style.css">

# composable-rss-engine

composable-rss is a multi-user, self-hosted platform that allows you to programmatically create, publish, and manage syndicated web feeds.

This repository contains the Engine server, which is a submodule of composable-rss-app. The engine is responsible for 
running periodically executing jobs, such as feed expiration/archival and scheduled deployment.

## 1. Quick-start using pre-built containers:

If you don't want to do development, just start ComposableRSS using pre-built containers:

```
docker ...
```

<hr>

## 3. For local development:

If you don't want to use the pre-built containers (i.e., you want to make custom code changes and build your own containers), then use the following instructions.

### Setup command aliases:

A script called `build_module.sh` is provided to expedite image assembly.  Setup command aliases to run it to build the required images after you make code changes:

```
alias crss-engine='./build_module.sh composable-rss-engine'
```

#### Alternately, setup aliases build debuggable containers:

```
alias crss-engine='./build_module.sh composable-rss-api --debug 55005'
```

*Debuggable containers pause on startup until a remote debugger is attached on the specified port.*

### Build and run:

#### Run the following command in the directory that contains ```composable-rss-engine```:

```
crss-engine && docker ...
```

Boot down in the regular way, by using ```docker ...``` in the ```composable-rss-engine``` directory.

<hr> 

You can also use the `crss-engine` alias to rebuild the container (i.e., to deploy code changes).

```
$ crss-engine # rebuild the engine server container 
```
