package com.jagex.js5.tools;

import com.jagex.js5.JS5Cache;
import com.jagex.js5.JS5Container;
import com.jagex.js5.JS5FileStore;
import com.jagex.js5.JS5ReferenceTable;
import com.jagex.js5.JS5ReferenceTable.Entry;
import com.jagex.js5.def.ClientScript;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ClientScriptDumper {

	private static final Map<Integer, String> opcodes = new HashMap<>();

	static {
		opcodes.put(0, "pushi");
		opcodes.put(1, "pushi_cfg");
		opcodes.put(2, "popi_cfg");
		opcodes.put(3, "pushs");
		opcodes.put(6, "goto");
		opcodes.put(7, "if_ne");
		opcodes.put(8, "if_eq");
		opcodes.put(9, "if_lt");
		opcodes.put(10, "if_gt");
		opcodes.put(21, "return");
		opcodes.put(25, "pushi_varbit");
		opcodes.put(26, "popi_varbit");
		opcodes.put(31, "if_lteq");
		opcodes.put(32, "if_gteq");
		opcodes.put(33, "loadi");
		opcodes.put(34, "storei");
		opcodes.put(35, "loads");
		opcodes.put(36, "stores");
		opcodes.put(37, "concat_str");
		opcodes.put(38, "popi");
		opcodes.put(39, "pops");
		opcodes.put(40, "call");
		opcodes.put(42, "loadi_global");
		opcodes.put(43, "storei_global");
		opcodes.put(44, "dim");
		opcodes.put(45, "push_array");
		opcodes.put(46, "pop_array");
		opcodes.put(47, "loads_global");
		opcodes.put(48, "stores_global");
		opcodes.put(51, "switch");
	}

	public static void main(String[] args) throws IOException {
		try (JS5Cache cache = new JS5Cache(JS5FileStore.open("../game/data/cache/"))) {
			JS5ReferenceTable rt = JS5ReferenceTable.decode(JS5Container.decode(cache.store().read(255, 12)).getData());
			for (int id = 0; id < rt.capacity(); id++) {
				Entry entry = rt.getEntry(id);
				if (entry == null)
					continue;

				ClientScript script = ClientScript.decode(cache.read(12, id).getData());
				System.out.println("===== " + id + " ======");
				for (int op = 0; op < script.getLength(); op++) {
					int opcode = script.getOpcode(op);

					String str = script.getStrOperand(op);
					int val = script.getIntOperand(op);

					String name = opcodes.get(opcode);
					if (name == null)
						name = "op" + opcode;

					String param = str != null ? str : Integer.toString(val);
					System.out.println(op + " " + name + " " + param);
				}
				System.out.println();
			}
		}
	}

}
