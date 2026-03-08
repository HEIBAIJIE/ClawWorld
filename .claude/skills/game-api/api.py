#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ClawWorld游戏API工具
提供登录和执行指令两个核心接口，自动处理战斗中的wait指令
"""

import requests
import sys
import platform
import io
import locale
import os

# 调试：显示编码信息
_original_stdout_encoding = sys.stdout.encoding
_system_encoding = locale.getpreferredencoding()

# 自适应平台编码
if platform.system() == "Windows":
    # 检测是否在Git Bash环境中（通过环境变量）
    is_git_bash = os.environ.get('MSYSTEM') or os.environ.get('TERM') == 'xterm'

    if is_git_bash:
        # Git Bash期望UTF-8编码
        try:
            sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
            sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')
        except:
            pass
    else:
        # 普通Windows终端使用系统编码
        try:
            if _system_encoding and _system_encoding.lower() not in ['utf-8', 'utf8']:
                sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding=_system_encoding, errors='replace')
                sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding=_system_encoding, errors='replace')
        except:
            pass

# 根据操作系统选择API请求编码
CHARSET = "gbk" if platform.system() == "Windows" else "utf-8"
BASE_URL = "http://localhost:8080"


def login(username, password):
    """
    登录或注册账号
    返回: {"success": bool, "sessionId": str, "response": str}
    """
    url = f"{BASE_URL}/api/auth/login"
    headers = {"Content-Type": f"application/json; charset={CHARSET}"}
    data = {"username": username, "password": password}

    try:
        response = requests.post(url, json=data, headers=headers)
        result = response.json()

        if result.get("success"):
            session_id = result.get("sessionId")
            background = result.get("backgroundPrompt", "")
            return {
                "success": True,
                "sessionId": session_id,
                "response": background
            }
        else:
            return {
                "success": False,
                "sessionId": None,
                "response": result.get("message", "登录失败")
            }
    except Exception as e:
        return {
            "success": False,
            "sessionId": None,
            "response": f"请求失败: {e}"
        }


def execute_command(session_id, command):
    """
    执行游戏指令，自动处理wait
    返回: {"success": bool, "response": str}
    """
    url = f"{BASE_URL}/api/command/execute"
    headers = {"Content-Type": f"application/json; charset={CHARSET}"}
    data = {"sessionId": session_id, "command": command}

    try:
        response = requests.post(url, json=data, headers=headers)
        result = response.json()

        response_text = result.get("response", "")

        # 自动处理wait
        if "未轮到你的回合，请输入wait继续等待" in response_text:
            return execute_command(session_id, "wait")

        return {
            "success": result.get("success", False),
            "response": response_text
        }
    except Exception as e:
        return {
            "success": False,
            "response": f"请求失败: {e}"
        }


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="ClawWorld游戏API命令行工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  python api.py login myuser mypass
  python api.py exec <sessionId> "register 战士 测试战士"
  python api.py exec <sessionId> "move 5 5"
        """
    )

    subparsers = parser.add_subparsers(dest='command', help='可用命令')

    # login 子命令
    login_parser = subparsers.add_parser('login', help='登录或注册账号')
    login_parser.add_argument('username', help='用户名')
    login_parser.add_argument('password', help='密码')

    # exec 子命令
    exec_parser = subparsers.add_parser('exec', help='执行游戏指令')
    exec_parser.add_argument('session_id', help='会话ID')
    exec_parser.add_argument('game_command', help='游戏指令')

    args = parser.parse_args()

    if not args.command:
        parser.print_help()
        sys.exit(1)

    if args.command == 'login':
        result = login(args.username, args.password)
        if result['success']:
            print(f"SessionId: {result['sessionId']}")
            print(f"\n{result['response']}")
            sys.exit(0)
        else:
            print(f"登录失败: {result['response']}", file=sys.stderr)
            sys.exit(1)

    elif args.command == 'exec':
        result = execute_command(args.session_id, args.game_command)
        if result['success']:
            print(result['response'])
            sys.exit(0)
        else:
            print(f"执行失败: {result['response']}", file=sys.stderr)
            sys.exit(1)
