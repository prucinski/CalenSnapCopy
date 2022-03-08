# Database

The development database is called 'calensnap_dev'.

# Deployment to Heroku

## Prerequisites

- [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)
- [Git](https://git-scm.com/downloads)
- A clone of this repository

After logging into your heroku account, first you need to link your local repository to the Heroku remote, using the command
`heroku git:remote -a calensnap-api`

## Deployment

First, commit any uncommitted changes.

Then, from the root directory of the repository run:

`git subtree push --prefix database heroku master`

This will push only the database (api)
subfolder to Heroku to be deployed.
