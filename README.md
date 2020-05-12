# Azure Storage Connector
Is an OpCon connector that can interact with blobs (files) in Microsoft Azure Storage.
![diagrm](/docs/images/Connector_overview.png)

The job definitions are entered as Windows jobs using the Azure Storage Job Sub-Type. When the job is scheduled by OpCon the arguments are passed to the connector and a completion code is returned.

The connectors supports the following tasks to manage coantainers and blobs (files).

- **Create Container**: creates a container in the storage account.
- **Delete Container**: deletes a container(s) from the storage account (supports wild cards ? and *).
- **List Container**: lists container(s) in the storage account (supports wild cards ? and *).
- **File Arrival**: monitors a container for the arrival of a blob (file).
- **File Delete**: deletes a blob(s) (file) from the container (supports wild cards ? and *).
- **File Download**: downloads a blob(s) (file) from the container to local storage (supports wild cards ? and *).
- **File Download**: downloads a blob(s) (file) from the container to local storage (supports wild cards ? and *).
- **File Upload**: uploads a blob(s) (file) from local storage to the container (supports wild cards ? and *).

# Prerequisites
- Microsoft Azure Account
- Execution requires a /java directory that contains a java 11 binary to execute the connector

# Instructions

For detailed information see the aszure-storage.md documentation.
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
