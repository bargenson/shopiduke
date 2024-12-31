# Shopify Java Library

This project is a Java library designed to facilitate the creation of Shopify apps in Java. It provides a servlet-based implementation of the OAuth flow of a Shopify app, a filter to limit resources to shops with known access tokens, and a basic GraphQL client to consume the Shopify Admin API.

## Features

- **OAuth Flow**: Servlet-based implementation of the OAuth flow for Shopify apps.
- **Access Control**: Filter to ensure only shops with access tokens can access protected resources.
- **GraphQL Client**: Basic client to interact with the Shopify Admin API.

## Usage Example

The project includes an example using Spring Boot and Jetty showing how it can be used on various projects.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven
- Shopify API credentials (API key, API secret, and scopes)

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/your-repo/shopify-java-library.git
   cd shopify-java-library
   ```

2. Install the dependencies:
  ```sh
   mvn clean install
  ```

4. Create a `shopifyapp.properties` file in the `src/main/resources` directory with the following content:
   ```sh
   shopify.client.id=your_api_key
   shopify.client.secret=your_api_secret
   shopify.client.scopes=read_products,write_products
   shopify.api.version=2024-10
   base.url=http://localhost:8080
   ```

### Running the Usage Example

1. Start using either of the following commands:
   ```sh
   mvn clean test-compile exec:java@jetty # Directly on a Servlet container
   mvn clean test-compile exec:java@spring-boot # With Spring Boot
   ```

2. The application will start and expose the following endpoints:
   - `/auth`: Handles Shopify OAuth authentication.
   - `/products`: Lists products from the Shopify store.

### Usage

- **Authentication**: Navigate to `http://localhost:8080/auth?shop=your-shop-name.myshopify.com` to authenticate with Shopify.
- **List Products**: Navigate to `http://localhost:8080/products?shop=your-shop-name.myshopify.com` to list products from the authenticated Shopify store.

### License

This project is licensed under the MIT License.
