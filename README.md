# Azure Storage Connector
OpCon connector to interact with files in Microsoft Azure Storage.

# Prerequisites
- Microsoft Azure Account
- Execution requires a /java directory that contains a java 1.8 binary to execute the connector

# Instructions
Provides following functions to manage containers and files.
 
Supports the following arguments:
- **-sa**: storage account name
- **-f**: function - group action, value:
  - **operations** or **information**
- **-o**: 
  - action values for **operations** group action
    - **containercreate**: creates a container in the storage account
    - **containerdelete**: deletes a container in the storage account
    - **filedelete**: deletes a file in a container
    - **filedownload**: downloads a file from the container to a local disk
    - **fileupload**: uploads a file from a local disk to a container
  - action values for **information** group action
    - **containers**: lists containers in the storage account
    - **blobs**: list blobs in containers in the storage account
- **-cn**: container name, used to define a container name associated with an action (supports * and ? wild cards)
- **-fn**: file name, used to define a file name associated with an action (supports * and ? wild cards)
- **-dir**: directory name, used to define upload or download directory name
- **-ov**: overwrite, used to indicate that if file exists during file upload it canbe overwritten

### Configuration:

The **Connector.config** file contains information about connecting to storage account. 
The \[STORAGE ACCOUNT\] section provides information about storage accounts that the connector can utilize.

Possible to define multiple values where:
- **name**: the name of a storage account
- **connection string**: the connection string associated with the azure storage account 

````
[CONNECTOR]
NAME=Azure Storage Connector
DEBUG=OFF

[STORAGE ACCOUNTS]
STORAGE=<name>=<connection string>
STORAGE=<name>=<connection string>
STORAGE=<name>=<connection string>
````
example:
````
MSAzureStorage.exe -sa test -f operations -o containercreate -cn ctest01
````

# Disclaimer
No Support and No Warranty are provided by SMA Technologies for this project and related material. The use of this project's files is on your own risk.

SMA Technologies assumes no liability for damage caused by the usage of any of the files offered here via this Github repository.


# License
Copyright 2019 SMA Technologies

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Contributing
We love contributions, please read our [Contribution Guide](CONTRIBUTING.md) to get started!

# Code of Conduct
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](code-of-conduct.md)
SMA Technologies has adopted the [Contributor Covenant](CODE_OF_CONDUCT.md) as its Code of Conduct, and we expect project participants to adhere to it. Please read the [full text](CODE_OF_CONDUCT.md) so that you can understand what actions will and will not be tolerated.
