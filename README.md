# Alexa Skill MyMemo PROTOTYPE
[![pipeline status master](https://gitlab.lrz.de/sweng/software-engineering-1/badges/master/pipeline.svg)](https://gitlab.lrz.de/sweng/software-engineering-1/-/commits/master) \
[![pipeline status develop](https://gitlab.lrz.de/sweng/software-engineering-1/badges/develop/pipeline.svg)](https://gitlab.lrz.de/sweng/software-engineering-1/-/commits/develop)

> ⚠️ This is a **Prototype**, you may encounter some undefined behaviour in some cases. ⚠️

## About

The Alexa Skill: 'MyMemo' offers users the ability to set reminders for future activities.  \
This Prototype was developed specifically for users diagnosed with dementia, and should assist the users in their daily lives as well as prolong their ability to lead a normal lifestyle.

With this skill you can store, view, edit and delete reminders.  \
Following are some of the advanced key features:
1. You have the possibility to share reminders so that other people can read (at the moment only reading is supported) your reminders.
2. You will be periodically reminded (i.e. every 5 minutes after you desired reminder time) until you confirm with the skill that you completed what you wanted to be reminded about.
3. You can create reminders that will be periodically repeated (i.e. you want to have a weekly reminder)

## Documents
This skill was developed using Amazons `Working Backwards` method.  \
In the folder `documents` you will find all necessary information about the development process of this skill.
- The prototypes source code which can be run on an AWS Lambda service.
- A Press Release and FAQ concerning MyMemo
- A Use-Case diagram
- An Activity diagram
- A Usercard containing information to get started using the skill.

## Building the app
### Prerequisites
#### Java 11
You can download the needed JDK [here](https://aws.amazon.com/de/corretto/). Or you can use any other way to download a
java 11 JDK (e.g. [sdkman](https://sdkman.io)).

### With IntelliJ
1. Go to the file `build.gradle`
2. Press on the `RUN`-button to the left of the `jar`-step.

### With terminal
#### MacOs/Unix
1. On the command line run `./gradlew jar`

#### Windows
1. On the command line run `./gradlew.bat jar`

## Hosting this skill by yourself
### Deploying the app to aws
After building with the `jar`-step you can find the jar, that you need to upload in `build/libs/xxx.jar`. \
After uploading the jar file you need to specify the default handler in AWS. This needs to be set to `edu.hm.sweng.SimpleAlexaSkillStreamHandler::handleRequest`.

### Configuring the skill in alexa developer console
Using the files mentioned below you can setup the skill in your [Amazon Developer account](https://developer.amazon.com). \
All necessary data can be found in `src/resources`.  \
`interactionModel.json`: You can import this json into your skill to get all intents with utterances.  \
`skill-config.json`: You can use this file together with the ask-cli to set up the basic skill settings (you may need to adjust the aws endpoints to the one you [deployed](#deploying-the-app-to-aws))

## Developers
Bryan J. Liegsalz  \
Konrad Moron  \
Christof Huber  \
Anonymous Student
