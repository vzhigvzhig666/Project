from pathlib import Path
import os
import shutil
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

SUITES = {
    "Unit": ("domain/build/test-results/desktopTest", "CourseTaskUseCasesTest"),
    "API": ("api-tests/build/test-results/test", "TodosApiTest"),
    "Compose": ("app/build/outputs/androidTest-results/connected", "AlkaaInstrumentedTest"),
    "UI Automator": ("app/build/outputs/androidTest-results/connected", "AlkaaBlackBoxTest"),
}


def collect_results():
    output = ROOT / "build" / "course-junit"
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)

    for suite, (directory, test_class) in SUITES.items():
        count = 0
        for index, path in enumerate(sorted((ROOT / directory).rglob("TEST-*.xml"))):
            for test_suite in ET.parse(path).getroot().iter("testsuite"):
                cases = [
                    case for case in test_suite.findall("testcase")
                    if case.get("classname", "").endswith("." + test_class)
                ]
                if not cases:
                    continue
                count += len(cases)
                test_suite.set("name", suite)
                for case in list(test_suite.findall("testcase")):
                    if case not in cases:
                        test_suite.remove(case)
                ET.ElementTree(test_suite).write(
                    output / f"{test_class}-{index}-{count}.xml",
                    encoding="utf-8",
                    xml_declaration=True,
                )
        if count == 0:
            raise SystemExit(f"{suite}: no results found in {directory}")
        print(f"{suite}: {count} test results", flush=True)
    return output


if __name__ == "__main__":
    results = collect_results()
    command = [os.environ["ALLURE_BIN"]] if "ALLURE_BIN" in os.environ else [
        "npx", "--yes", "allure@3.16.1",
    ]
    subprocess.run(
        [*command, "generate", str(results), "-o", str(ROOT / "reports" / "allure")],
        cwd=ROOT,
        check=True,
    )
