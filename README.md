# CraftyStates

CraftyStates is a gradle plugin that act as a transpiler between 'smart' blockstates jsons and Forge format.

## Installing

Add the plugin and the maven repository containing it to the buildscript:
```gradle
buildscript {
    repositories {
        jcenter()
        maven { url = "http://files.minecraftforge.net/maven" }
        maven { url = "http://maven.ferenyr.info/artifactory/opmcorp"}
    }
    dependencies {
        classpath 'net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT'
        classpath 'net.opmcorp:craftystates:0.1.0'
    }
}
```

Apply the plugin right after the forgegradle one:
```gradle
apply plugin: 'net.minecraftforge.gradle.forge'
apply plugin: 'net.opmcorp.craftystates'
```

Configure the path where your blockstates files are located and pretty printing:
```gradle
statesTranspiling {
    blockstatesPath = "src/main/resources/assets/examplemod/blockstates/"
    prettyPrinting = true
}
```

## Usage
Create a file named <blockstate name>.cs.json then add the marker `craftystates_marker` in it.
The current version of the format is 1.

Inside the .cs.json file any valid forge blockstate syntax can be used and mixed with the CraftyStates syntax.

Blockstates generation happens before the gradle task `processResources`, to generate the blockstates before testing you can add the task `blockstatesTranspiling` as a pre-launch command in your IDE.

## Features

### Default values infering

Forge
```json
  "defaults": {
    "model": "cube_all",
    "textures": {},
    "transform": "forge:default-block"
  }
```
CraftyStates
```json
  "defaults": {
    "model": "cube_all"
  }
```

### Redundant model specification

Forge
```json
{
  "defaults": {
    "model": "cube_all",
    "textures": {},
    "transform": "forge:default-block"
  },
  "variants": {
    "type=hard": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/hardblock"
      }
    },
    "type=soft": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/softblock"
      }
    }
  }
}
```
CraftyStates
```json
{
  "defaults": {
    "model": "cube_all"
  },
  "variants": {
    "type=hard": {
      "textures": {
        "all": "examplemod:blocks/hardblock"
      }
    },
    "type=soft": {
      "textures": {
        "all": "examplemod:blocks/softblock"
      }
    }
  }
}
```

### Textures block expansion

Forge
```json
  "textures": {
    "all": "examplemod:blocks/dummyblock"
  }
```
CraftyStates
```json
  "texture#all": "examplemod:blocks/dummyblock"
```

### Variants matcher and value replacement

Forge
```json
    "type=sandstone,mood=happy": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/happy_sandstone"
      }
    },
    "type=sandstone,mood=sad": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/sad_sandstone"
      }
    },
    "type=stone,mood=happy": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/happy_stone"
      }
    },
    "type=stone,mood=sad": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/sad_stone"
      }
    }
```
CraftyStates
```json
  "matcher": {
      "type": ["sandstone", "stone"],
      "mood": ["happy", "sad"],
      "values": {
        "texture#all": "examplemod:blocks/#mood_#type"
      }
    }
```

## Example
<details>
<summary>This snippet use all the current features of the transpiler</summary>

```json
{
  "craftystates_marker": 1,
  "defaults": {
    "model": "cube_all"
  },
  "variants": {
    "inventory": {
      "texture#all": "examplemod:blocks/happy_stone"
    },
    "matcher": {
      "type": ["sandstone", "stone"],
      "mood": ["happy", "sad"],
      "values": {
        "texture#all": "examplemod:blocks/#mood_#type"
      }
    }
  }
}
```
</details>


<details>
<summary>The produced output is</summary>

```json
{
  "defaults": {
    "model": "cube_all",
    "textures": {},
    "transform": "forge:default-block"
  },
  "variants": {
    "inventory": {
      "textures": {
        "all": "examplemod:blocks/happy_stone"
      }
    },
    "type=sandstone,mood=happy": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/happy_sandstone"
      }
    },
    "type=sandstone,mood=sad": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/sad_sandstone"
      }
    },
    "type=stone,mood=happy": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/happy_stone"
      }
    },
    "type=stone,mood=sad": {
      "model": "cube_all",
      "textures": {
        "all": "examplemod:blocks/sad_stone"
      }
    }
  },
  "forge_marker": 1
}
```
</details>
