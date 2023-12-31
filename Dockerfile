# Use a base Node.js image with the desired version
FROM node:14

# Install Java SDK (Version 11) from Adoptium
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk

# Create a directory for your app
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install npm dependencies
RUN npm install

# Copy the rest of the application code to the working directory
COPY . .

# Build the Shadow CLJS project
RUN npx shadow-cljs release client server

# Expose the port your application will run on
EXPOSE 3000

# Command to start the Node.js server
command ["node", "out/server/main.js"]
