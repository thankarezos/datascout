# Use the official Python 3.11 image as a parent image
FROM python:3.11

# Set the working directory to /app
WORKDIR /

# Copy the current directory contents into the container at /app
COPY ./Pipfile Pipfile

RUN pip install --upgrade pip && \
    pip install pipenv

RUN pipenv install --deploy --ignore-pipfile;


# Command to run the application
ENTRYPOINT ["pipenv", "run", "python", "/app/model.py"]


