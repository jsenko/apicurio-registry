
.PHONY: clean
clean:
	rm -rf openapi.json apicurioregistrysdk/client kiota_tmp .venv dist

.PHONY: install
install:
	poetry install

.PHONY: test
test:
	poetry run pytest -s

.PHONY: lint-check
lint-check:
	poetry run black ./ --check

.PHONY: lint-apply
lint-apply:
	poetry run black ./

.PHONY: build
build: install
	poetry build

.PHONY: publish
publish: install
	poetry publish --build -u __token__ -p ${PYPI_TOKEN}

.PHONY: update
update:
	poetry add microsoft-kiota-abstractions@latest
	poetry add microsoft-kiota-http@latest
	poetry add microsoft-kiota-serialization-json@latest
	poetry add microsoft-kiota-serialization-text@latest
	poetry add microsoft-kiota-serialization-form@latest
	poetry add microsoft-kiota-serialization-multipart@latest
