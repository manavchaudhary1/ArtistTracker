<h1 align="center">Artist Tracker</h1>

## Overview
Artist Tracker is a Java Application designed to help users check their favorite artists and about their latest releases.
<br/>

## Table of Contents
- [Requirements](#requirements)
- [Technologies Used](#technologies)
- [Installation](#installation)
- [Backup and Restore](#backup)
- [Supported Sites](#sites)
- [Features](#features)
- [Contributing](#contributing)

<a name="requirements"></a>
## Requirements
- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/)

<a name="technologies"></a>
## Technologies Used
| Technology                                                | Purpose                                     |
|-----------------------------------------------------------|---------------------------------------------|
| [Spring Boot](https://spring.io/projects/spring-boot)     | Backend framework for building RESTful APIs |
| [JSP](https://en.wikipedia.org/wiki/Jakarta_Server_Pages) | For Single Page Dashboard                   |
| [H2](https://www.h2database.com/html/main.html)           | Relational SQL Database for persistent Data |

<a name="installation"></a>
## Installation
- Clone the repository:
    ```bash
        git clone https://github.com/yourusername/ArtistTracker.git
        cd ArtistTracker
    ```
- Build the project
   ```bash
      mvn clean install
   ``` 
- Run the application 
    ```bash
        mvn spring-boot:run
    ```
  **OR**

- Run the JAR file
    ```bash
        java -jar target/ArtistTracker-v1.jar
    ```
- Open your web browser and navigate to `http://localhost:8080/`

<a name="backup"></a>
## Backup and Restore
To back up or migrate your artist list, simply copy the db-backup.zip file from the `src/main/resources` directory to your desired location. You can restore it by placing the zip file back into the same directory and Running the application.

<a name="sites"></a>
## Supported Sites
- [Hitomi](https://hitomi.la/)

> **_NOTE:_** Write now only hitomi is supported, as it is primary source for other sites like [nhentai](https://nhentai.net/) or [hentaifox](https://hentaifox.com).
> > More Sites can be added in the future, feel free to open an issue or pull request if you want to add a new site.


<a name="features"></a>
## Features
- **Track Multiple Artists:** Add any artist from supported platforms and see their latest works in one dashboard.
- **Instant Refresh:** Quickly check for new works for all tracked artists or individual artists.
- **Responsive UI:** Works across desktops, tablets, and mobile devices.
- **Light/Dark/Auto Theme:** Enjoy a theme that matches your device or personal preference.
- **Easy Image Previews:** See gallery thumbnails and open the original gallery in one click.
- **Persistent Storage:** Back up and restore artist lists as needed.

<a name="contributing"></a>
## Contributing
Feel free to open issues or submit pull requests for improvements/fixes!