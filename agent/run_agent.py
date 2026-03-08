#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ClawWorld 智能体运行脚本
支持多配置、会话管理、自动记忆压缩
"""

import json
import sys
import time
import argparse
import os
from pathlib import Path
from datetime import datetime

# Windows 下启用 ANSI 颜色码支持
if sys.platform == 'win32':
    try:
        import ctypes
        kernel32 = ctypes.windll.kernel32
        kernel32.SetConsoleMode(kernel32.GetStdHandle(-11), 7)
    except:
        pass

# 导入游戏API
from api import login, execute_command

try:
    import requests
except ImportError:
    print("错误: 需要安装 requests 库")
    print("请运行: pip install requests")
    sys.exit(1)


# ANSI 颜色码
class Colors:
    RESET = '\033[0m'
    BOLD = '\033[1m'

    # 前景色
    BLACK = '\033[30m'
    RED = '\033[31m'
    GREEN = '\033[32m'
    YELLOW = '\033[33m'
    BLUE = '\033[34m'
    MAGENTA = '\033[35m'
    CYAN = '\033[36m'
    WHITE = '\033[37m'
    GRAY = '\033[90m'

    # 亮色
    BRIGHT_RED = '\033[91m'
    BRIGHT_GREEN = '\033[92m'
    BRIGHT_YELLOW = '\033[93m'
    BRIGHT_BLUE = '\033[94m'
    BRIGHT_MAGENTA = '\033[95m'
    BRIGHT_CYAN = '\033[96m'


class Logger:
    """日志工具类"""

    debug_mode = False  # 调试模式开关

    @staticmethod
    def _get_timestamp():
        """获取时间戳"""
        return datetime.now().strftime("%H:%M:%S")

    @staticmethod
    def _format_log(log_type, color, message, show_separator=False):
        """格式化日志"""
        timestamp = Logger._get_timestamp()
        if show_separator:
            print(f"\n{Colors.GRAY}{'─' * 80}{Colors.RESET}", flush=True)
        print(f"{Colors.GRAY}[{timestamp}]{Colors.RESET} {color}{Colors.BOLD}[{log_type}]{Colors.RESET} {message}", flush=True)

    @staticmethod
    def debug(message):
        """调试日志（仅在调试模式下显示）"""
        if Logger.debug_mode:
            Logger._format_log("调试", Colors.GRAY, message)

    @staticmethod
    def thinking(message):
        """智能体思考日志"""
        Logger._format_log("思考", Colors.YELLOW, message)

    @staticmethod
    def decision(message):
        """智能体决策日志"""
        Logger._format_log("决策", Colors.BRIGHT_GREEN, message)

    @staticmethod
    def response(message, truncate=True):
        """服务器响应日志"""
        if truncate and len(message) > 500:
            # 如果响应太长，截断显示
            display_msg = message[:500] + f"\n{Colors.GRAY}... (响应过长，已截断，完整内容已发送给智能体){Colors.RESET}"
        else:
            display_msg = message
        Logger._format_log("响应", Colors.BRIGHT_BLUE, f"\n{display_msg}")

    @staticmethod
    def system(message):
        """系统日志"""
        Logger._format_log("系统", Colors.CYAN, message)

    @staticmethod
    def error(message):
        """错误日志"""
        Logger._format_log("错误", Colors.BRIGHT_RED, message)

    @staticmethod
    def turn_separator(turn_count):
        """回合分隔符"""
        print(f"\n{Colors.BOLD}{Colors.MAGENTA}{'═' * 80}{Colors.RESET}", flush=True)
        print(f"{Colors.BOLD}{Colors.MAGENTA}第 {turn_count} 轮{Colors.RESET}", flush=True)
        print(f"{Colors.BOLD}{Colors.MAGENTA}{'═' * 80}{Colors.RESET}\n", flush=True)


class GameAgent:
    def __init__(self, config_path):
        """初始化智能体"""
        self.config = self.load_config(config_path)
        self.session_id = None
        self.conversation_history = []
        self.turn_count = 0
        self.system_prompt = self.build_system_prompt()

    def load_config(self, config_path):
        """加载配置文件"""
        Logger.debug(f"正在加载配置文件: {config_path}")
        try:
            with open(config_path, 'r', encoding='utf-8') as f:
                config = json.load(f)

            # 验证必需字段
            required_fields = ['username', 'password', 'game_goal', 'behavior_style',
                             'llm_base_url', 'llm_api_key', 'llm_model']
            for field in required_fields:
                if field not in config:
                    raise ValueError(f"配置文件缺少必需字段: {field}")

            # 设置默认值
            config.setdefault('max_history_turns', 50)
            config.setdefault('compress_interval', 50)
            config.setdefault('game_server_url', 'http://localhost:8080')
            config.setdefault('llm_timeout', 180)  # 默认180秒超时

            Logger.debug("配置加载成功")
            return config
        except Exception as e:
            Logger.error(f"加载配置文件失败: {e}")
            import traceback
            traceback.print_exc()
            sys.exit(1)

    def build_system_prompt(self):
        """构建[REDACTED]"""
        # 读取 AGENT_PROMPT.md
        prompt_path = Path(__file__).parent / 'AGENT_PROMPT.md'
        Logger.debug(f"正在读取提示词文件: {prompt_path}")
        try:
            with open(prompt_path, 'r', encoding='utf-8') as f:
                prompt = f.read()

            # 替换占位符
            prompt = prompt.replace('{{GAME_GOAL}}', self.config['game_goal'])
            prompt = prompt.replace('{{BEHAVIOR_STYLE}}', self.config['behavior_style'])

            Logger.debug("[REDACTED]构建成功")
            return prompt
        except Exception as e:
            Logger.error(f"读取[REDACTED]失败: {e}")
            import traceback
            traceback.print_exc()
            sys.exit(1)

    def login_game(self):
        """登录游戏"""
        Logger.system(f"正在登录游戏: {self.config['username']}...")
        result = login(self.config['username'], self.config['password'])

        if result['success']:
            self.session_id = result['sessionId']
            Logger.system(f"登录成功! SessionId: {self.session_id}")
            Logger.response(result['response'], truncate=False)

            # 初始化对话历史
            self.conversation_history = [
                {"role": "system", "content": self.system_prompt},
                {"role": "user", "content": f"游戏开始。服务器响应:\n{result['response']}"}
            ]
            return True
        else:
            Logger.error(f"登录失败: {result['response']}")
            return False

    def call_llm(self):
        """调用大模型获取下一步指令"""
        api_type = self.config.get('api_type', 'ollama')

        if api_type == 'ollama':
            return self.call_llm_ollama()
        else:
            return self.call_llm_openai()

    def call_llm_ollama(self):
        """调用 Ollama 原生 API"""
        url = f"{self.config['llm_base_url']}/chat"
        enable_think = self.config.get('enable_think', False)

        payload = {
            "model": self.config['llm_model'],
            "messages": self.conversation_history,
            "stream": True,
            "think": enable_think
        }

        try:
            timeout = self.config.get('llm_timeout', 180)
            response = requests.post(url, json=payload, stream=True, timeout=timeout)
            response.raise_for_status()

            full_content = ""

            # 逐行读取流式响应
            for line in response.iter_lines():
                if line:
                    data = json.loads(line.decode('utf-8'))

                    # 处理思考过程（如果启用）
                    if 'think' in data and data['think']:
                        Logger.debug(f"[模型思考] {data['think']}")

                    # 处理正式回复
                    if 'message' in data and 'content' in data['message']:
                        content = data['message']['content']
                        full_content += content

                    # 检查是否完成
                    if data.get('done', False):
                        break

            return full_content if full_content else None

        except KeyboardInterrupt:
            Logger.system("用户中断操作")
            raise
        except requests.exceptions.Timeout:
            Logger.error(f"大模型响应超时（{self.config.get('llm_timeout', 180)}秒）")
            Logger.system("提示：如果使用本地大模型，可能需要增加 llm_timeout 配置")
            return None
        except Exception as e:
            Logger.error(f"调用 Ollama API 失败: {e}")
            return None

    def call_llm_openai(self):
        """调用 OpenAI 兼容 API"""
        url = f"{self.config['llm_base_url']}/chat/completions"
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.config['llm_api_key']}"
        }
        data = {
            "model": self.config['llm_model'],
            "messages": self.conversation_history,
            "temperature": 0.7
        }

        try:
            timeout = self.config.get('llm_timeout', 180)
            response = requests.post(url, json=data, headers=headers, timeout=timeout)
            response.raise_for_status()
            result = response.json()

            assistant_message = result['choices'][0]['message']['content']
            return assistant_message
        except KeyboardInterrupt:
            Logger.system("用户中断操作")
            raise
        except requests.exceptions.Timeout:
            Logger.error(f"大模型响应超时（{self.config.get('llm_timeout', 180)}秒）")
            Logger.system("提示：如果使用本地大模型，可能需要增加 llm_timeout 配置")
            return None
        except Exception as e:
            Logger.error(f"调用 OpenAI API 失败: {e}")
            return None

    def parse_agent_response(self, response):
        """解析智能体响应的JSON"""
        try:
            # 尝试提取JSON（可能包含在markdown代码块中）
            response = response.strip()
            if response.startswith('```'):
                # 移除markdown代码块标记
                lines = response.split('\n')
                response = '\n'.join(lines[1:-1])

            data = json.loads(response)
            return data.get('thinking', ''), data.get('command', '')
        except Exception as e:
            Logger.error(f"解析响应失败: {e}")
            Logger.error(f"原始响应: {response}")
            return None, None

    def execute_game_command(self, command):
        """执行游戏指令"""
        result = execute_command(self.session_id, command)
        return result['success'], result['response']

    def compress_history(self):
        """压缩对话历史"""
        Logger.system("对话历史达到上限，正在压缩...")

        # 提取需要压缩的对话（保留系统消息）
        system_msg = self.conversation_history[0]
        to_compress = self.conversation_history[1:-10]  # 保留最近10轮
        recent = self.conversation_history[-10:]

        # 构建压缩请求
        compress_prompt = """请将以下游戏对话历史压缩成简短的记忆摘要（200字以内），保留关键信息：
- 当前角色状态（等级、职业、位置）
- 重要的战斗经历和结果
- 获得的关键装备和物品
- 与其他玩家的重要互动
- 当前的主要目标和计划

对话历史:
"""
        for msg in to_compress:
            compress_prompt += f"\n{msg['role']}: {msg['content'][:200]}..."

        compress_messages = [
            {"role": "system", "content": "你是一个游戏记忆压缩助手，负责提取关键信息。"},
            {"role": "user", "content": compress_prompt}
        ]

        url = f"{self.config['llm_base_url']}/chat/completions"
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.config['llm_api_key']}"
        }
        data = {
            "model": self.config['llm_model'],
            "messages": compress_messages,
            "temperature": 0.3
        }

        try:
            response = requests.post(url, json=data, headers=headers, timeout=60)
            response.raise_for_status()
            result = response.json()
            summary = result['choices'][0]['message']['content']

            # 重建对话历史
            self.conversation_history = [
                system_msg,
                {"role": "user", "content": f"[历史记忆摘要]\n{summary}\n\n[继续游戏]"},
                *recent
            ]

            Logger.system(f"压缩完成，历史记忆:\n{summary}")
        except Exception as e:
            Logger.error(f"压缩失败: {e}，保留最近对话")
            self.conversation_history = [system_msg, *recent]

    def run(self):
        """运行智能体主循环"""
        # 登录游戏
        if not self.login_game():
            return

        Logger.system("智能体开始运行，按 Ctrl+C 停止")
        Logger.system("=" * 60)

        try:
            while True:
                self.turn_count += 1
                Logger.turn_separator(self.turn_count)

                # 调用大模型
                Logger.system("正在调用大模型...")
                try:
                    llm_response = self.call_llm()
                except KeyboardInterrupt:
                    raise

                if not llm_response:
                    Logger.error("大模型调用失败，等待5秒后重试...")
                    time.sleep(5)
                    continue

                # 解析响应
                thinking, command = self.parse_agent_response(llm_response)
                if not command:
                    Logger.error("无法解析指令，等待5秒后重试...")
                    time.sleep(5)
                    continue

                # 打印日志
                Logger.thinking(thinking)
                Logger.decision(command)

                # 记录助手响应
                self.conversation_history.append({
                    "role": "assistant",
                    "content": llm_response
                })

                # 执行游戏指令
                success, game_response = self.execute_game_command(command)
                Logger.response(game_response)

                # 记录游戏响应
                self.conversation_history.append({
                    "role": "user",
                    "content": f"指令执行结果:\n{game_response}"
                })

                # 检查是否需要压缩历史
                if len(self.conversation_history) > self.config['max_history_turns']:
                    self.compress_history()

                # 短暂延迟，避免请求过快
                time.sleep(2)

        except KeyboardInterrupt:
            Logger.system(f"\n智能体已停止，总共运行了 {self.turn_count} 轮")


def main():
    Logger.debug("脚本启动")

    parser = argparse.ArgumentParser(
        description="ClawWorld 智能体运行脚本",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  python run_agent.py                          # 使用默认配置
  python run_agent.py -c config/aggressive.json  # 使用激进型配置
  python run_agent.py -c config/social.json      # 使用社交型配置
  python run_agent.py --debug                  # 启用调试模式
        """
    )

    parser.add_argument(
        '-c', '--config',
        default='config/default.json',
        help='配置文件路径（默认: config/default.json）'
    )

    parser.add_argument(
        '--debug',
        action='store_true',
        help='启用调试模式'
    )

    args = parser.parse_args()

    # 设置调试模式
    if args.debug:
        Logger.debug_mode = True

    Logger.debug(f"命令行参数解析完成，配置文件: {args.config}")

    # 检查配置文件是否存在
    config_path = Path(__file__).parent / args.config
    Logger.debug(f"完整配置路径: {config_path}")

    if not config_path.exists():
        Logger.error(f"配置文件不存在: {config_path}")
        print("\n可用的配置文件:", flush=True)
        config_dir = Path(__file__).parent / 'config'
        if config_dir.exists():
            for f in config_dir.glob('*.json'):
                print(f"  - config/{f.name}", flush=True)
        sys.exit(1)

    Logger.debug("配置文件存在，开始创建智能体")

    # 创建并运行智能体
    try:
        agent = GameAgent(config_path)
        Logger.debug("智能体创建成功，开始运行")
        agent.run()
    except KeyboardInterrupt:
        Logger.system("\n用户中断，智能体已停止")
        sys.exit(0)
    except Exception as e:
        Logger.error(f"运行失败: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
