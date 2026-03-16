from playwright.sync_api import sync_playwright

def run():
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        
        # 捕获 console 事件
        page.on("console", lambda msg: print(f"Browser Console {msg.type}: {msg.text}"))
        # 捕获 pageerror 事件
        page.on("pageerror", lambda err: print(f"Browser PageError: {err.message}"))
        
        page.goto("http://localhost:3000")
        page.wait_for_timeout(3000)
        browser.close()

if __name__ == "__main__":
    run()
