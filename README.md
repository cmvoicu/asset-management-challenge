# asset-management-challenge
The Asset Management Digital  Dev Challenge

#### Points to be improved and make the application production-ready: ####
* refactor AccountsService.getAccount to return Optional as it may return null.
* use a generic way to define transfer(class hierarchy) and some renaming of the service, in case more transactions are added
   like payment, withdrawal, account balance info
   here also maybe threat transfer like a resource to store/list executed transfers as rest API
* check code coverage and metrics using Sonar
* add code formatter for the project
* perform code review
* write a stress test
* perform some manual dev tests on DEV environment after deploy.
* real life application will not store, in this case, the data in memory in a map, but will require a storage(DB)
    * this will introduce transactions
* add some monitoring for the application:
         --number of transfers/transactions executed
         --average request time
         --server load(disk, memory, CPU usage)

#### NON RELATED to the feature developed ####
* gracefull shutdown of the application and exception handling in main at startup.
* develop shell scripts for starting/stoping the application
* prepare the application for deploy, property placeholders





