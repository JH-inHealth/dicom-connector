DICOM Anypoint Connector
========================
Connector that implements [Query/Retrieve Service Class](https://dicom.nema.org/medical/dicom/current/output/html/part04.html#chapter_C) operations.  
Copyright (c) 2022 The Johns Hopkins University  
David J. Talley, [Technology Innovation Center](https://tic.jh.edu), [Precision Medicine Analytics Platform](https://pm.jh.edu)

Configurations
--------------
### DICOM Provider
Configures a Service Class Provider connection, used by the `Store SCP` source.

#### Parameters
| Tab         | Group        | Parameter           | Default   | Description                                                                                             |
|:------------|:-------------|:--------------------|:----------|:--------------------------------------------------------------------------------------------------------|
| General     | Local Server | AE Title            |           | Application Entity Title                                                                                |
| General     | Local Server | Hostname            | `0.0.0.0` | Hostname or IP Address                                                                                  |
| General     | Local Server | Port                | `104`     | Port Number                                                                                             |
| Buffer      |              | Max ops invoked     | `0`       |                                                                                                         |
| Buffer      |              | Max ops performed   | `0`       |                                                                                                         |
| Buffer      |              | Receive PDU Length  | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      |              | Send PDU Length     | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      |              | Send buffer size    | `0`       |                                                                                                         |
| Buffer      |              | Receive buffer size | `0`       |                                                                                                         |
| TLS Context |              | TLS Configuration   | `None`    | See [TLS Context](#tls-context)                                                                         |
| Timings     |              | Connection timeout  | `0`       |                                                                                                         |
| Timings     |              | Request timeout     | `0`       |                                                                                                         |
| Timings     |              | Accept timeout      | `0`       |                                                                                                         |
| Timings     |              | Release timeout     | `0`       |                                                                                                         |
| Timings     |              | Send timeout        | `0`       |                                                                                                         |
| Timings     |              | Response timeout    | `0`       |                                                                                                         |
| Timings     |              | Idle timeout        | `0`       |                                                                                                         |
| Timings     |              | Socket close delay  | `50`      |                                                                                                         |

### DICOM User
Configures a Service Class User connection, used by all of the `SCU` operations.

| Tab         | Group         | Parameter           | Default   | Description                                                                                             |
|:------------|:--------------|:--------------------|:----------|:--------------------------------------------------------------------------------------------------------|
| General     |               | Local AE Title      |           | Application Entity Title of the SCU                                                                     |
| General     | Remote Server | AE Title            |           | Application Entity Title of the SCP                                                                     |
| General     | Remote Server | Hostname            | `0.0.0.0` | Hostname or IP Address of the SCP                                                                       |
| General     | Remote Server | Port                | `104`     | Port Number of the SCP                                                                                  |
| Buffer      |               | Max ops invoked     | `0`       |                                                                                                         |
| Buffer      |               | Max ops performed   | `0`       |                                                                                                         |
| Buffer      |               | Receive PDU Length  | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      |               | Send PDU Length     | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      |               | Send buffer size    | `0`       |                                                                                                         |
| Buffer      |               | Receive buffer size | `0`       |                                                                                                         |
| Security    |               | Username            |           |                                                                                                         |
| Security    |               | Password            |           |                                                                                                         |
| TLS Context |               | TLS Configuration   | `None`    | See [TLS Context](#tls-context)                                                                         |
| Timings     |               | Connection timeout  | `0`       |                                                                                                         |
| Timings     |               | Request timeout     | `0`       |                                                                                                         |
| Timings     |               | Accept timeout      | `0`       |                                                                                                         |
| Timings     |               | Release timeout     | `0`       |                                                                                                         |
| Timings     |               | Send timeout        | `0`       |                                                                                                         |
| Timings     |               | Response timeout    | `0`       |                                                                                                         |
| Timings     |               | Idle timeout        | `0`       |                                                                                                         |
| Timings     |               | Socket close delay  | `50`      |                                                                                                         |

### DICOM Transfer
Configures two Service Class User connections, used by the `Transfer` operation. 
The Source Server parts of the configuration are used to connect to a remote DICOM server to perform C-GET operations.
The Target Server parts of the configuration are used to connect to a remote DICOM server to perform C-STORE operations.

| Tab         | Group                  | Parameter                 | Default   | Description                                                                                             |
|:------------|:-----------------------|:--------------------------|:----------|:--------------------------------------------------------------------------------------------------------|
| General     |                        | Local AE Title            |           | Application Entity Title of the SCU                                                                     |
| General     | Source Server          | AE Title                  |           | Application Entity Title of the source SCP                                                              |
| General     | Source Server          | Hostname                  | `0.0.0.0` | Hostname or IP Address of the source SCP                                                                |
| General     | Source Server          | Port                      | `104`     | Port Number of the source SCP                                                                           |
| General     | Target Server          | AE Title                  |           | Application Entity Title of the target SCP                                                              |
| General     | Target Server          | Hostname                  | `0.0.0.0` | Hostname or IP Address of the target SCP                                                                |
| General     | Target Server          | Port                      | `104`     | Port Number of the target SCP                                                                           |
| Buffer      | Source Server Buffer   | Max ops invoked           | `0`       |                                                                                                         |
| Buffer      | Source Server Buffer   | Max ops performed         | `0`       |                                                                                                         |
| Buffer      | Source Server Buffer   | Receive PDU Length        | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      | Source Server Buffer   | Send PDU Length           | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      | Source Server Buffer   | Send buffer size          | `0`       |                                                                                                         |
| Buffer      | Source Server Buffer   | Receive buffer size       | `0`       |                                                                                                         |
| Buffer      | Target Server Buffer   | Max ops invoked           | `0`       |                                                                                                         |
| Buffer      | Target Server Buffer   | Max ops performed         | `0`       |                                                                                                         |
| Buffer      | Target Server Buffer   | Receive PDU Length        | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      | Target Server Buffer   | Send PDU Length           | `16378`   | See [Protocol Data Unit](https://dicom.nema.org/medical/dicom/current/output/html/part08.html#sect_9.3) |
| Buffer      | Target Server Buffer   | Send buffer size          | `0`       |                                                                                                         |
| Buffer      | Target Server Buffer   | Receive buffer size       | `0`       |                                                                                                         |
| Security    | Source Server Security | Username                  |           |                                                                                                         |
| Security    | Source Server Security | Password                  |           |                                                                                                         |
| Security    | Target Server Security | Username                  |           |                                                                                                         |
| Security    | Target Server Security | Password                  |           |                                                                                                         |
| TLS Context |                        | TLS Configuration         | `None`    | See [TLS Context](#tls-context)                                                                         |
| TLS Context |                        | Use TLS Context in Source | `False`   |                                                                                                         |
| TLS Context |                        | Use TLS Context in Target | `False`   |                                                                                                         |
| Timings     | Source Server Timings  | Connection timeout        | `0`       |                                                                                                         |
| Timings     | Source Server Timings  | Request timeout           | `0`       |                                                                                                         |
| Timings     | Source Server Timings  | Accept timeout            | `0`       |                                                                                                         |
| Timings     | Source Server Timings  | Release timeout           | `0`       |                                                                                                         |
| Timings     | Source Server Timings  | Send timeout              | `0`       |                                                                                                         |
| Timings     | Source Server Timings  | Response timeout          | `0`       |                                                                                                         |
| Timings     | Source Server Timings  | Idle timeout              | `0`       |                                                                                                         |
| Timings     | Source Server Timings  | Socket close delay        | `50`      |                                                                                                         |
| Timings     | Target Server Timings  | Connection timeout        | `0`       |                                                                                                         |
| Timings     | Target Server Timings  | Request timeout           | `0`       |                                                                                                         |
| Timings     | Target Server Timings  | Accept timeout            | `0`       |                                                                                                         |
| Timings     | Target Server Timings  | Release timeout           | `0`       |                                                                                                         |
| Timings     | Target Server Timings  | Send timeout              | `0`       |                                                                                                         |
| Timings     | Target Server Timings  | Response timeout          | `0`       |                                                                                                         |
| Timings     | Target Server Timings  | Idle timeout              | `0`       |                                                                                                         |
| Timings     | Target Server Timings  | Socket close delay        | `50`      |                                                                                                         |

Source
------
### Store SCP
Performs C-STORE as a Service Class Provider (Listener).

#### Parameters
| Tab      | Group          | Parameter               | Default | Description                                                                                        |
|:---------|:---------------|:------------------------|:--------|:---------------------------------------------------------------------------------------------------|
| General  | Basic Settings | Connector configuration |         | See [DICOM Provider](#dicom-provider)                                                              |
| General  | General        | Storage SOP Classes     | `None`  | `None` will accept all classes. See [Storage SOP Classes](#storage-sop-classes) for more details.  |

#### Outbound Payload
| Data Type     | Media Type         | Description                   |
|:--------------|:-------------------|:------------------------------|
| `DicomObject` | `application/java` | [DICOM Object](#dicom-object) |

### Get SCU Results
Receives each image from the `Get SCU to Flow` operation. See [Get SCU to Flow](#get-scu-to-flow) for more details.

#### Outbound Attributes
| Name       | Description                                                               |
|:-----------|:--------------------------------------------------------------------------|
| action     | `start`, `receive`, or `stop`                                             |
| seriesName | Value from the `Series name` parameter of the `Get SCU to Flow` operation |
| iuid       | Instance UID of the DICOM image                                           |

#### Outbound Payload
| Data Type     | Media Type         | Description                   |
|:--------------|:-------------------|:------------------------------|
| `DicomObject` | `application/java` | [DICOM Object](#dicom-object) |

Operations
----------
### Apply Preamble
Adds a preamble to a DICOM object.

#### Parameters
| Tab     | Group   | Parameter    | Default      | Description                       |
|:--------|:--------|:-------------|:-------------|:----------------------------------|
| General | General | DICOM Object | `#[payload]` | See [DICOM Object](#dicom-object) |
| General | General | Change Tags  |              | See [Change Tags](#change-tags)   |

#### Outbound Payload
| Data Type               | Media Type          | Description                                |
|:------------------------|:--------------------|:-------------------------------------------|
| `ByteArrayOutputStream` | `application/dicom` | File suitable for storage on a file system |

### Echo SCU
Performs C-ECHO as a Service Class User with a remote Application Entity.

#### Parameters
| Tab       | Group          | Parameter               | Default | Description                   |
|:----------|:---------------|:------------------------|:--------|:------------------------------|
| General   | Basic Settings | Connector configuration |         | See [DICOM User](#dicom-user) |

#### Output Payload
`EchoScuPayload` object with the following properties:

| Property Name | Type     |
|:--------------|:---------|
| messageId     | `int`    |

### Extract Tags
Extract tags from a DICOM InputStream (serialized org.dcm4che3.data.Attributes object) into #\[attributes]

#### Parameters
| Tab       | Group    | Parameter    | Default      | Description                               |
|:----------|:---------|:-------------|:-------------|:------------------------------------------|
| General   | General  | DICOM Object | `#[payload]` | See [DICOM Object](#dicom-object)         |
| General   | General  | Tag Names    |              | List of [Tag Identities](#tag-identities) |

#### Output Payload
| Data Type                 | Media Type         | Description                                                                                                                  |
|:--------------------------|:-------------------|:-----------------------------------------------------------------------------------------------------------------------------|
| `Map<String, DicomValue>` | `application/java` | A Map of Name/Value tags, where the name is a [Tag Identity](#tag-identities) and the value is a [Dicom Value](#dicom-value) |

### Find SCU
Performs C-FIND as a Service Class User with a remote Application Entity.
Accepts a Map of query parameters (see below) to perform a query, returning results in an array of maps.

#### Parameters
| Tab                  | Group                | Parameter               | Default          | Description                                                                           |
|:---------------------|:---------------------|:------------------------|:-----------------|:--------------------------------------------------------------------------------------|
| General              | Basic Settings       | Connector configuration |                  | See [DICOM User](#dicom-user)                                                         |
| General              | Search               | Search Keys             | `#[payload]`     | See [Search Keys](#search-keys)                                                       |
| General              | Search               | Response Tags           |                  | List of [Tag Identities](#tag-identities) to get in the response                      |
| Presentation Context | Presentation Context | Information Model       | `STUDY_ROOT`     |                                                                                       |
| Presentation Context | Presentation Context | Retrieve Level          |                  |                                                                                       |
| Presentation Context | Presentation Context | Transfer Syntax         | `IMPLICIT_FIRST` |                                                                                       |
| Timings              | Timings              | Cancel After            | `0`              | Milliseconds to wait on each operation before throwing DICOM:CANCELED (0 is infinite) |

#### Output Payload
`FindScuPayload` object with the following properties: 

| Property Name             | Type                            |
|:--------------------------|:--------------------------------|
| messageId                 | `int`                           |
| messageIdBeingRespondedTo | `int`                           |
| affectedSOPClassUID       | `String`                        |
| results                   | `List<Map<String, DicomValue>>` |

Each result is a Map of Name/Value tags, where the name is a [Tag Identity](#tag-identities) and the value is a [Dicom Value](#dicom-value)

### Get SCU to File System
Performs C-GET as a Service Class User with a remote Application Entity.
Saves each DICOM file to a folder, returning an ArrayList of their fully qualified filenames.

#### Parameters
| Tab                  | Group                | Parameter               | Default          | Description                                                                           |
|:---------------------|:---------------------|:------------------------|:-----------------|:--------------------------------------------------------------------------------------|
| General              | Basic Settings       | Connector configuration |                  | See [DICOM User](#dicom-user)                                                         |
| General              | General              | Folder Name             |                  |                                                                                       |
| General              | General              | Compress files          | `False`          | Will stream all received images into a TAR/GZIP file                                  |
| General              | Search               | Search Keys             | `#[payload]`     | See [Search Keys](#search-keys)                                                       |
| General              | Search               | Storage SOP Classes     | `None`           | See [Storage SOP Classes](#storage-sop-classes)                                       |
| Presentation Context | Presentation Context | Information Model       | `STUDY_ROOT`     |                                                                                       |
| Presentation Context | Presentation Context | Retrieve Level          |                  |                                                                                       |
| Presentation Context | Presentation Context | Transfer Syntax         | `IMPLICIT_FIRST` |                                                                                       |
| Timings              | Timings              | Store Timeout           | `0`              |                                                                                       |
| Timings              | Timings              | Cancel After            | `0`              | Milliseconds to wait on each operation before throwing DICOM:CANCELED (0 is infinite) |

#### Output Payload
`GetScuPayload` object with the following properties:

| Property Name                  | Type           |
|:-------------------------------|:---------------|
| messageId                      | `int`          |
| messageIdBeingRespondedTo      | `int`          |
| affectedSOPClassUID            | `String`       |
| numberOfCompletedSuboperations | `int`          |
| numberOfFailedSuboperations    | `int`          |
| numberOfRemainingSuboperations | `int`          |
| numberOfWarningSuboperations   | `int`          |
| filenames                      | `List<String>` |

### Get SCU to Object Store
Performs C-GET as a Service Class User with a remote Application Entity.
Saves each DICOM file to an Object Store as a byte array, returning an ArrayList of their keys (in the `filenames` property).

#### Parameters
| Tab                  | Group                | Parameter               | Default          | Description                                                                                   |
|:---------------------|:---------------------|:------------------------|:-----------------|:----------------------------------------------------------------------------------------------|
| General              | Basic Settings       | Connector configuration |                  | See [DICOM User](#dicom-user)                                                                 |
| General              | General              | Object store            |                  |                                                                                               |
| General              | General              | Key name prefix         |                  | Prefix to use for each key name. Each file's Instance UID will be appended following a colon. |
| General              | Search               | Search Keys             | `#[payload]`     | See [Search Keys](#search-keys)                                                               |
| General              | Search               | Storage SOP Classes     | `None`           | See [Storage SOP Classes](#storage-sop-classes)                                               |
| Presentation Context | Presentation Context | Information Model       | `STUDY_ROOT`     |                                                                                               |
| Presentation Context | Presentation Context | Retrieve Level          |                  |                                                                                               |
| Presentation Context | Presentation Context | Transfer Syntax         | `IMPLICIT_FIRST` |                                                                                               |
| Timings              | Timings              | Store Timeout           | `0`              |                                                                                               |
| Timings              | Timings              | Cancel After            | `0`              | Milliseconds to wait on each operation before throwing DICOM:CANCELED (0 is infinite)         |

#### Output Payload
`GetScuPayload` object with the following properties:

| Property Name                  | Type           |
|:-------------------------------|:---------------|
| messageId                      | `int`          |
| messageIdBeingRespondedTo      | `int`          |
| affectedSOPClassUID            | `String`       |
| numberOfCompletedSuboperations | `int`          |
| numberOfFailedSuboperations    | `int`          |
| numberOfRemainingSuboperations | `int`          |
| numberOfWarningSuboperations   | `int`          |
| filenames                      | `List<String>` |

### Get SCU to Flow
Performs C-GET as a Service Class User with a remote Application Entity.
When the operation starts, it sends an empty message to the `Get SCU Results` listener with the `attributes.action` of `start`.
Each DICOM file that is received is sent to the `Get SCU Results` listener with the `attributes.action` of `receive`.
After the last image is received, it sends an empty message to the `Get SCU Results` listener with the `attributes.action` of `stop`.

#### Parameters
| Tab                  | Group                | Parameter               | Default          | Description                                                                                                                            |
|:---------------------|:---------------------|:------------------------|:-----------------|:---------------------------------------------------------------------------------------------------------------------------------------|
| General              | Basic Settings       | Connector configuration |                  | See [DICOM User](#dicom-user)                                                                                                          |
| General              | General              | Target flow name        |                  | Name of a Flow that has a [Get SCU Results](#get-scu-results) listener as its source.                                                  |
| General              | General              | Series name             |                  | A string that is included as an attribute to the target flow. Intended to be used to identify all of the images returned by the C-GET. |
| General              | Search               | Search Keys             | `#[payload]`     | See [Search Keys](#search-keys)                                                                                                        |
| General              | Search               | Storage SOP Classes     | `None`           | See [Storage SOP Classes](#storage-sop-classes)                                                                                        |
| Presentation Context | Presentation Context | Information Model       | `STUDY_ROOT`     |                                                                                                                                        |
| Presentation Context | Presentation Context | Retrieve Level          |                  |                                                                                                                                        |
| Presentation Context | Presentation Context | Transfer Syntax         | `IMPLICIT_FIRST` |                                                                                                                                        |
| Timings              | Timings              | Store Timeout           | `0`              |                                                                                                                                        |
| Timings              | Timings              | Cancel After            | `0`              | Milliseconds to wait on each operation before throwing DICOM:CANCELED (0 is infinite)                                                  |

#### Output Payload
`GetScuPayload` object with the following properties:

| Property Name                  | Type           |
|:-------------------------------|:---------------|
| messageId                      | `int`          |
| messageIdBeingRespondedTo      | `int`          |
| affectedSOPClassUID            | `String`       |
| numberOfCompletedSuboperations | `int`          |
| numberOfFailedSuboperations    | `int`          |
| numberOfRemainingSuboperations | `int`          |
| numberOfWarningSuboperations   | `int`          |
| filenames                      | `List<String>` |

### Move SCU
Performs C-MOVE as a Service Class User with a remote Application Entity.

#### Parameters
| Tab                  | Group                | Parameter               | Default          | Description                                                                           |
|:---------------------|:---------------------|:------------------------|:-----------------|:--------------------------------------------------------------------------------------|
| General              | Basic Settings       | Connector configuration |                  | See [DICOM User](#dicom-user)                                                         |
| General              | Search               | Search Keys             | `#[payload]`     | See [Search Keys](#search-keys)                                                       |
| General              | Search               | Storage SOP Classes     | `None`           | See [Storage SOP Classes](#storage-sop-classes)                                       |
| Presentation Context | Presentation Context | Information Model       | `STUDY_ROOT`     |                                                                                       |
| Presentation Context | Presentation Context | Retrieve Level          |                  |                                                                                       |
| Presentation Context | Presentation Context | Transfer Syntax         | `IMPLICIT_FIRST` |                                                                                       |
| Timings              | Timings              | Store Timeout           | `0`              |                                                                                       |
| Timings              | Timings              | Cancel After            | `0`              | Milliseconds to wait on each operation before throwing DICOM:CANCELED (0 is infinite) |

#### Output Payload
`MoveScuPayload` with the following properties:

| Properties Name                | Type     |
|:-------------------------------|:---------|
| messageId                      | `int`    |
| messageIdBeingRespondedTo      | `int`    |
| affectedSOPClassUID            | `String` |
| numberOfCompletedSuboperations | `int`    |
| numberOfFailedSuboperations    | `int`    |
| numberOfRemainingSuboperations | `int`    |
| numberOfWarningSuboperations   | `int`    |

### Read File from File System
Reads a DICOM file into a [DICOM InputStream](#dicom-object).

#### Parameters
| Tab      | Group    | Parameter | Default | Description              |
|:---------|:---------|:----------|:--------|:-------------------------|
| General  | General  | Filename  |         | Filename of a DICOM file |

#### Output Payload
| Data Type     | Media Type         | Description                       |
|:--------------|:-------------------|:----------------------------------|
| `DicomObject` | `application/java` | See [DICOM Object](#dicom-object) |

### Read File from Object Store
Reads a DICOM file into a [DICOM InputStream](#dicom-object).

#### Parameters
| Tab      | Group    | Parameter    | Default | Description                  |
|:---------|:---------|:-------------|:--------|:-----------------------------|
| General  | General  | Object store |         |                              |
| General  | General  | Key Name     |         | Key Name in the Object Store |

#### Output Payload
| Data Type     | Media Type         | Description                       |
|:--------------|:-------------------|:----------------------------------|
| `DicomObject` | `application/java` | See [DICOM Object](#dicom-object) |

### Store File to File System
Saves a [DICOM Object](#dicom-object) as a DICOM file

#### Parameters
| Tab      | Group         | Parameter           | Default                           | Description                       |
|:---------|:--------------|:--------------------|:----------------------------------|:----------------------------------|
| General  | General       | Folder Name         |                                   | Where to save the file            |
| General  | General       | Filename            | `Random UUID with .dcm extension` |                                   |
| General  | General       | DICOM Object        | `#[payload]`                      | See [DICOM Object](#dicom-object) |
| General  | General       | Change Tags         |                                   | See [Change Tags](#change-tags)   |

#### Output Payload
| Data Type | Media Type         | Description              |
|:----------|:-------------------|:-------------------------|
| `String`  | `application/java` | Fully qualified filename |

### Store File to Object Store
Saves a [DICOM Object](#dicom-object) as a DICOM file (byte array) in an Object Store

#### Parameters
| Tab      | Group     | Parameter    | Default      | Description                       |
|:---------|:----------|:-------------|:-------------|:----------------------------------|
| General  | General   | Object store |              | Where to save the file            |
| General  | General   | Key Name     |              | Key Name in the Object Store      |
| General  | General   | DICOM Object | `#[payload]` | See [DICOM Object](#dicom-object) |
| General  | General   | Change Tags  |              | See [Change Tags](#change-tags)   |

#### Output Payload
| Data Type | Media Type         | Description              |
|:----------|:-------------------|:-------------------------|
| `String`  | `application/java` | Fully qualified filename |

### Store SCU
Performs C-STORE as a Service Class User with a remote Application Entity.

#### Parameters
| Tab      | Group                           | Parameter               | Default      | Description                                                                                                     |
|:---------|:--------------------------------|:------------------------|:-------------|:----------------------------------------------------------------------------------------------------------------|
| General  | Basic Settings                  | Connector configuration |              | See [DICOM User](#dicom-user)                                                                                   |
| General  | General                         | Delete Source Files     | `False`      | Deletes the source files after transfer. Ignored when the source comes from DICOM Object.                       |
| General  | General                         | Change Tags             |              | See [Change Tags](#change-tags)                                                                                 |
| General  | DICOM Image Source (Choose One) | DICOM Object            | `#[payload]` | See [DICOM Object](#dicom-object)                                                                               |
| General  | DICOM Image Source (Choose One) | Filename                |              | File must be DICOM, GZIP of a DICOM, or TAR/GZIP of a collection of DICOM files                                 |
| General  | DICOM Image Source (Choose One) | Folder Name             |              | Will process all files that are DICOM, GZIP of a DICOM, or TAR/GZIP of a collection of DICOM files              |
| General  | DICOM Image Source (Choose One) | List of Files           | `None`       | Array must be a Folder Name or Filename of a DICOM, GZIP of a DICOM, or TAR/GZIP of a collection of DICOM files |
| General  | DICOM Image Source (Choose One) | Object store            |              | All keys from the object store will be extracted                                                                |
| Timings  | Timings                         | Cancel After            | `0`          | Milliseconds to wait on each operation before throwing DICOM:CANCELED (0 is infinite)                           |

#### Output Payload
| Data Type      | Media Type         | Description                                                 |
|:---------------|:-------------------|:------------------------------------------------------------|
| `List<String>` | `application/java` | List of the SOP Instance UID of each DICOM file transferred |

### Transfer
Performs C-GET with a source Application Entity and C-STORE on each received DICOM file to a target Application Entity.

#### Parameters
| Tab                  | Group                | Parameter               | Default          | Description                                                                           |
|:---------------------|:---------------------|:------------------------|:-----------------|:--------------------------------------------------------------------------------------|
| General              | Basic Settings       | Connector configuration |                  | See [DICOM Transfer](#dicom-transfer)                                                 |
| General              | General              | Change Tags             |                  | See [Change Tags](#change-tags)                                                       |
| General              | Search               | Search Keys             | `#[payload]`     | See [Search Keys](#search-keys)                                                       |
| General              | Search               | Storage SOP Classes     | `None`           | See [Storage SOP Classes](#storage-sop-classes)                                       |
| Presentation Context | Presentation Context | Information Model       | `STUDY_ROOT`     |                                                                                       |
| Presentation Context | Presentation Context | Retrieve Level          |                  |                                                                                       |
| Presentation Context | Presentation Context | Transfer Syntax         | `IMPLICIT_FIRST` |                                                                                       |
| Timings              | Timings              | Cancel After            | `0`              | Milliseconds to wait on each operation before throwing DICOM:CANCELED (0 is infinite) |

#### Outbound Payload
| Data Type      | Media Type         | Description                                                 |
|:---------------|:-------------------|:------------------------------------------------------------|
| `List<String>` | `application/java` | List of the SOP Instance UID of each DICOM file transferred |

Common Parameters
-----------------
### DICOM Object
This `DicomObject` object holds the DICOM file metadata found in the preamble as well as the actual DICOM content, which is technically an `org.dcm4che3.data.Attributes` object from the DCM4Che library.

| Field                        | Data Type  |
|:-----------------------------|:-----------|
| transferSyntaxUid            | String     |
| sourceApplicationEntityTitle | String     |
| implementationClassUid       | String     |
| implementationVersionName    | String     |
| content                      | Attributes |

### Search Keys
Search keys are used to query a remote AET for operations such as Find SCU, Get SCU, and Transfer.
It's a name/value map where the name is a `Tag Identity` (see definition below) and its search value.

Example:
```
{ "PatientID": "PAT01", "AccessionNumber": "0050" }
```

### Change Tags
This is a name/value map where the name is a `Tag Identity` (see definition below). Each tag in the map will be set on all of the DICOM files processed by the operation.
If the tag does not exist, it will create it. If the tag already exists, it will overwrite it. You can also reference a value from another tag using spring-like property
replacement. For example, `${StudyDate}_${AccessionNumber}` would set a tag to the value of StudyDate concatenated with '_' and the value of AccessionNumber.

### Tag Identities
Registered data elements can be found at [Registry of DICOM Data Elements](https://dicom.nema.org/medical/dicom/current/output/html/part06.html#chapter_6). 
Whenever you need to reference a `tag identity`, you can use one of the following formats.
Note that you cannot use the keyword format for unregistered data elements such as private tags.

| Format    | Example      | RegEx Pattern                        | Description                                         |
|:----------|:-------------|:-------------------------------------|:----------------------------------------------------|
| Hex       | "0x00100020" | ^0[xX][0-9a-fA-F]{8}$                | 0x followed by the full 8-digit HEX number.         |
| Hex Pair  | "0010,0020"  | ^([0-9a-fA-F]{4})\W([0-9a-fA-F]{4})$ | A pair of 4-digit HEX numbers separated by a comma. |
| Integer   | "1048608"    | ^\d$                                 | The base-10 value of the full 8-digit HEX number.   |
| Keyword   | "PatientID"  |                                      | The Keyword of a registered data element.           |

Example query using Keywords:
```
{ "PatientID": "PAT01", "AccessionNumber": "0050" }
```

Example query using Hex Pairs:
```
{ "0010,0020": "PAT01", "0008,0050": "0050" }
```

### Dicom Value
This `DicomValue` object can hold the value of a variety of primitive data types. The `type` field is an enum of type `DicomValueType`.

| Value of `type` field | Native Data Type          | Method to retrieve value | Compatible methods                             |
|:----------------------|:--------------------------|:-------------------------|:-----------------------------------------------|
| DOUBLE                | `double`                  | asDouble                 | asString                                       |
| FLOAT                 | `float`                   | asFloat                  | asString, asDouble                             |
| INTEGER               | `int`                     | asInteger                | asString, asLong, asDouble, asFloat            |
| LIST                  | `List<DicomValue>`        | asList                   |                                                |
| LONG                  | `long`                    | asLong                   | asString                                       |
| MAP                   | `Map<String, DicomValue>` | asMap                    |                                                |
| SHORT                 | `short`                   | asShort                  | asString, asLong, asInteger, asDouble, asFloat |
| STRING                | `String`                  | asString                 |                                                |
| NULL                  | n/a                       | n/a                      |                                                |


### Storage SOP Classes
A list of Storage [Service-Order Pair (SOP) Classes](https://www.dicomlibrary.com/dicom/sop/) and their supported [Transfer Syntax UIDs](https://www.dicomlibrary.com/dicom/transfer-syntax/) 
is used to communicate what SOP Classes and Transfer Syntaxes are supported. DICOM limits the number of SOP classes you can specify to 128.

SOP Classes and Transfer Syntaxes can be specified using either their UID value (e.g. 1.2.840.10008.1.2) or their UID keyword (e.g. ImplicitVRLittleEndian) as defined in DICOM Standard Part 6  
[Registry of DICOM Unique Identifiers](https://dicom.nema.org/medical/dicom/current/output/html/part06.html#table_A-1). 
In the map, the SOP Class is the property key and the Transfer Syntaxes are listed in the property value as an array of string.

| Description                                 | Example                                                                                                          |
|:--------------------------------------------|:-----------------------------------------------------------------------------------------------------------------|
| UID strings with array of transfer syntaxes | `{ "1.2.840.10008.5.1.4.1.1.88.11": ["1.2.840.10008.1.2","1.2.840.10008.1.2.1","1.2.840.10008.1.2.1.99"] }`      |
| Class names with array of transfer syntaxes | `{ "BasicTextSRStorage": ["ImplicitVRLittleEndian","ExplicitVRLittleEndian","DeflatedExplicitVRLittleEndian"] }` |

### TLS Context
#### TLS Configuration for Providers (SCP)
- The Key Store must be defined and contain the server's private key pair
- The Trust Store must be defined (can be same as the key store)
- When `Insecure` is not selected, the Trust Store _must_ contain the public certificates of all client SCUs

#### TLS Configuration for Users (SCU)
- The Trust Store must be defined and contain the SCP server's public certificate
- The `Insecure` option is ignored
- If the SCP server requires client identity, the Key Store must be defined and contain the client's private key pair

Exceptions
----------

| Exception Type             | Description                                                                                              |
|:---------------------------|:---------------------------------------------------------------------------------------------------------|
| DICOM:CLIENT_SECURITY      | Security errors from an SCU operation                                                                    |
| DICOM:CONNECTIVITY         | An IOException occurred when communicating with the remote AET                                           |
| DICOM:CANCELED             | The cancelAfter timer expired, causing the SCU operation to terminate                                    |
| DICOM:SSL                  | An SSL exception when when using TLS.                                                                    |
| DICOM:REQUEST_ERROR        | Error message received from the remote AET, usually indicating that something was wrong with the request |
| DICOM:FILE_IO              | An IOException occurred when working with files (e.g. reading or writing on the file system)             |
| DICOM:INVALID_DICOM_OBJECT | The `DICOM Object` parameter must be of type `DicomObject`                                               |
| DICOM:MISSING_UID          | A UID needed for processing is missing from the DICOM file                                               |
| DICOM:NOT_FOUND            | The Get SCU, Find SCU, or Transfer query does not retrieve any results from the remote AET               |
| DICOM:SERVER_SECURITY      | Security errors from Store SCP                                                                           |

Mule supported versions
-----------------------
MuleSoft Runtime 4.1+

Maven Configuration
-------------------
### pom.xml
Add repository locations
```
<repositories>
    <repository>
        <id>dcm4che</id>
        <name>DCM4Che Repository</name>
        <url>https://dcm4che.org/maven2/</url>
    </repository>
    <repository>
        <id>github</id>
        <name>GitHub JH-inHealth</name>
        <url>https://maven.pkg.github.com/JH-inHealth/dicom-connector</url>
    </repository>
</repositories>
```

### settings.xml
Add a GitHub username and personal access token
```
<servers>
    <id>github</id>
    <username>USERNAME</username>
    <password>TOKEN</password>
</servers>
```

References
-----------------------
- [DCM4Che Source Code](https://github.com/dcm4che/dcm4che/blob/master/README.md)
- [DICOM Standard](https://www.dicomstandard.org/current/)
- [DICOM Library](https://www.dicomlibrary.com/dicom/)
- [GitHub Maven Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

License
-------
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)  
[Creative Commons Attribution 4.0 International License](http://creativecommons.org/licenses/by/4.0/)