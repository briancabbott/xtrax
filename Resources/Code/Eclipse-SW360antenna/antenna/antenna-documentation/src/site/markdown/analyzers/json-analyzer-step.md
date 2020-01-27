## JSON analyzer
This analyzer investigates a given *json* file. The json file contains the components of the project. The component consists
of a hash value, identifier, path to the file, proprietary state and license data. The identifier consists of a format and
coordinates, which specifies the type of the component. An example declaration can be seen below.


```json
{
  "components": [
    {
      "hash": "b2921b7862e7b26b43aa",
      "componentIdentifier": {
        "format": "maven",
        "coordinates": {
          "artifactId": "commons-lang3",
          "groupId": "org.apache.commons",
          "version": "3.5"
        }
      },
      "proprietary": false,
      "matchState": "exact",
      "pathnames": [
        "commons-lang-2.0.jar"
      ],
      "licenseData": {
        "declaredLicenses": [
          {
            "licenseId": "Apache-2.0",
            "licenseName": "Apache License, v2.0"
          }
        ],
        "observedLicenses": [
          {
            "licenseId": "Apache-1.1",
            "licenseName": "Apache License, v1.1"
          }
        ],
        "overriddenLicenses": []
      }
    }
  ]
}
```

#### Explanation of parameters
* `components`: Array of different components.
* `hash`: Describes the component with a unique hash value.
* `componentIdentifier`: This object holds the **format** and **coordinates** object
* `format`: Describes the type of the coordinates with "maven", "nuget" or "a-name".
* `coordinates`: Depending on the format, it specifies the coordinates of the component.
    - `maven`:
        * `artifactId`: Generally the name that the project is known by.
        * `groupId`: Generally unique name amongst an organization or project is known by.
        * `version`: The version number of the component
    - `nuget`:
        * `packageId`: The name of the .NET package.
        * `version`: The version number of the package.
    - `a-name`:
        * `name`: The name of the component.
        * `version`: The version of the component.
* `proprietary`: Is the component a non-free software or not.
* `matchState`: Verify if the comparison of component to known components is or is not a match.
* `pathnames`: Paths to the components binaries.
* `licenseData`: This object holds the arrays **declaredLicenses**, **observedLicenses** and **overriddenLicenses**
* `declaredLicenses`: Any license(s) that has been declared by the author.
* `observedLicenses`: Any license(s) found during the scan of the component’s source code.
* `overriddenLicenses`: Any license(s) which should replace one of the above.


### How to use
Add the following step into the `<analyzers>` section of your workflow.xml

```xml
<step>
    <name>JSON Analyzer</name>
    <classHint>org.eclipse.sw360.antenna.workflow.analyzer.JsonAnalyzer</classHint>
    <analyzerConfiguration>
        <entry key="file.path" value="${basedir}/ClmReportData.json" />
        <entry key="base.dir" value="${project.build.directory}/sources" />
    </configuration>
</step>
```

#### Explanation of parameters
* `file.path`: Destination of a JSON file that matches the above format.
* `base.path`: Destination to the the source files that the JSON report refers to.

### Special license fields

There are number of special strings which can be used to convey failure messages using the "licenseId" field:

* `Not-Declared`: Only within `declaredLicenses`. No license was declared by the project (via package managers for instance), although some license text is configured or found.
* `No-Sources`: Only within `observedLicenses`. No sources are provided for the component so it's license could not be checked.
* `No-Source-License`: Only within `observedLicenses`. Sources for the component are available, but license information in sources is missing or incomplete.
* `Not-Provided`: Only within `declaredLicenses`. No license was provided by the project (via package managers for instance).
* `Not-Supported`: Only within `observedLicenses`. This should only be used by scanning tools. It means that license information is not handled by the scanner and must be checked differently.
* `Non-Standard`: A license is configured which is of non-standard license threat group.
