FROM scratch
WORKDIR /app
COPY target/native-executable ./application
EXPOSE 8080
ENTRYPOINT ["./application"]
