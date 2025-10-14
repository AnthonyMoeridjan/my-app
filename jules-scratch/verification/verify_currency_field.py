from playwright.sync_api import sync_playwright, expect

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    try:
        page.goto("http://localhost:8080/login")
        page.get_by_label("Username").fill("user")
        page.get_by_label("Password").fill("user")
        page.get_by_role("button", name="Log in").click()

        page.goto("http://localhost:8080/accounts-payable")
        page.get_by_role("button", name="New Bill").click()

        expect(page.get_by_label("Currency")).to_be_visible()
        page.screenshot(path="jules-scratch/verification/bill-currency-field.png")

    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)