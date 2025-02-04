# Opensource-statistiques 📊

🚧 This project is in progress ... 

One objective of this project is to highlight Zenika members for your open source contributions. 

To do this, the zenika-open-source GitHub Organization is scanned and data are be saved in a private Database on GCP.
Datas could be used to find and list projects maintained or forked by Zenika Members, all projects which Zenika members contributed, etc.

## 🗄️ Tech 

Using :
- [Quarkus](https://quarkus.io/)
- [Firestore](https://docs.quarkiverse.io/quarkus-google-cloud-services/) to save data
- [GitHub actions](https://github.com/features/actions) as CICD - 🚧 not implemented

## 🌐 API 

Some resources are available but only the first is in progress : 
- `/github/` to get information about Zenika Open Source GitHub organization and members from GitHub
- `/gitlab/` to get information about Zenika Open Source GitHub organization and members from GitLab 🚧 not implemented
- `/members/` to get information from GCP database 🚧 not implemented
- `/contributions/` to get information about contributions from GCP database 🚧 not implemented
- `/workflow/` to get data and save them on GCP database

> 🎯 The last one resource will be removed after implementing all features and will be replaced by schedules. 

## 📝 Setup 

- You need a GitHub token you can generate on [this page](https://github.com/settings/tokens).
- Create a `.env` file based on the `.env-example` file and set the token previously created. 
- You have to create a `.gcloud-conf.json` to save service account (it's not the best option actually, this can be replaced by another configuration).
- Run the application with `quarkus dev` if you have the [Quarkus CLI](https://quarkus.io/guides/cli-tooling) installed on your environment, or `mvn quarkus:dev`command.
- You can use Quarkus dev service for Firestore enabling this variable `quarkus.google.cloud.firestore.devservice.enabled` in application.properties file.


## ✨Contribute 

Anyone can contribute to this project. For the moment, please add your question or purpose something in [a new issue](https://github.com/zenika-open-source/opensource-statistics/issues).

![with love by zenika](https://img.shields.io/badge/With%20%E2%9D%A4%EF%B8%8F%20by-Zenika-b51432.svg?link=https://oss.zenika.com)
