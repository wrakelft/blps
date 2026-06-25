from pathlib import Path
import shutil
import xml.etree.ElementTree as ET
from collections import defaultdict, deque

BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL"
CAMUNDA = "http://camunda.org/schema/1.0/bpmn"
BPMNDI = "http://www.omg.org/spec/BPMN/20100524/DI"
DC = "http://www.omg.org/spec/DD/20100524/DC"
DI = "http://www.omg.org/spec/DD/20100524/DI"

ET.register_namespace("bpmn", BPMN)
ET.register_namespace("camunda", CAMUNDA)
ET.register_namespace("bpmndi", BPMNDI)
ET.register_namespace("dc", DC)
ET.register_namespace("di", DI)

NODE_TAGS = {
    f"{{{BPMN}}}startEvent",
    f"{{{BPMN}}}endEvent",
    f"{{{BPMN}}}serviceTask",
    f"{{{BPMN}}}userTask",
    f"{{{BPMN}}}exclusiveGateway",
    f"{{{BPMN}}}parallelGateway",
    f"{{{BPMN}}}inclusiveGateway",
    f"{{{BPMN}}}intermediateCatchEvent",
    f"{{{BPMN}}}intermediateThrowEvent",
    f"{{{BPMN}}}scriptTask",
    f"{{{BPMN}}}sendTask",
    f"{{{BPMN}}}receiveTask",
    f"{{{BPMN}}}manualTask",
    f"{{{BPMN}}}businessRuleTask",
}

CIRCLE_TAGS = {
    f"{{{BPMN}}}startEvent",
    f"{{{BPMN}}}endEvent",
    f"{{{BPMN}}}intermediateCatchEvent",
    f"{{{BPMN}}}intermediateThrowEvent",
}

GATEWAY_TAGS = {
    f"{{{BPMN}}}exclusiveGateway",
    f"{{{BPMN}}}parallelGateway",
    f"{{{BPMN}}}inclusiveGateway",
}


def safe_id(s: str) -> str:
    return "".join(c if c.isalnum() or c == "_" else "_" for c in s)


def node_size(tag):
    if tag in CIRCLE_TAGS:
        return 36, 36
    if tag in GATEWAY_TAGS:
        return 50, 50
    return 150, 80


def add_bounds(shape, x, y, w, h):
    ET.SubElement(shape, f"{{{DC}}}Bounds", {
        "x": str(int(x)),
        "y": str(int(y)),
        "width": str(int(w)),
        "height": str(int(h)),
    })


def add_waypoint(edge, x, y):
    ET.SubElement(edge, f"{{{DI}}}waypoint", {
        "x": str(int(x)),
        "y": str(int(y)),
    })


def find_process(root):
    processes = root.findall(f"{{{BPMN}}}process")
    if not processes:
        return None
    return processes[0]


def compute_layout(process, nodes, flows):
    node_ids = list(nodes.keys())
    outgoing = defaultdict(list)
    incoming_count = defaultdict(int)

    for flow in flows:
        source = flow.get("sourceRef")
        target = flow.get("targetRef")
        if source in nodes and target in nodes:
            outgoing[source].append(target)
            incoming_count[target] += 1
            incoming_count.setdefault(source, incoming_count.get(source, 0))

    starts = [
        nid for nid, el in nodes.items()
        if el.tag == f"{{{BPMN}}}startEvent"
    ]

    if not starts:
        starts = [nid for nid in node_ids if incoming_count.get(nid, 0) == 0]

    if not starts and node_ids:
        starts = [node_ids[0]]

    depth = {nid: 0 for nid in starts}
    q = deque(starts)
    seen = set(starts)

    while q:
        cur = q.popleft()
        for nxt in outgoing.get(cur, []):
            next_depth = depth[cur] + 1
            if next_depth > depth.get(nxt, -1):
                depth[nxt] = next_depth
            if nxt not in seen:
                seen.add(nxt)
                q.append(nxt)

    max_depth = max(depth.values()) if depth else 0

    for nid in node_ids:
        if nid not in depth:
            max_depth += 1
            depth[nid] = max_depth

    by_depth = defaultdict(list)
    for nid in node_ids:
        by_depth[depth[nid]].append(nid)

    order_index = {nid: i for i, nid in enumerate(node_ids)}

    for d in by_depth:
        by_depth[d].sort(key=lambda nid: order_index[nid])

    positions = {}

    base_x = 140
    step_x = 210
    base_y = 130
    step_y = 150

    for d in sorted(by_depth):
        group = by_depth[d]
        n = len(group)

        for idx, nid in enumerate(group):
            w, h = node_size(nodes[nid].tag)
            x = base_x + d * step_x
            y = base_y + (idx - (n - 1) / 2) * step_y
            positions[nid] = (x, y, w, h)

    return positions


def fix_file(path: Path):
    tree = ET.parse(path)
    root = tree.getroot()

    process = find_process(root)
    if process is None:
        print(f"SKIP no process: {path.name}")
        return

    process_id = process.get("id") or path.stem

    nodes = {}

    for el in list(process):
        if el.tag in NODE_TAGS and el.get("id"):
            nodes[el.get("id")] = el

    flows = [
        el for el in process.findall(f"{{{BPMN}}}sequenceFlow")
        if el.get("id")
    ]

    if not nodes:
        print(f"SKIP no nodes: {path.name}")
        return

    # Удаляем старую/битую визуальную разметку и генерируем новую.
    for child in list(root):
        if child.tag == f"{{{BPMNDI}}}BPMNDiagram":
            root.remove(child)

    positions = compute_layout(process, nodes, flows)

    diagram = ET.SubElement(root, f"{{{BPMNDI}}}BPMNDiagram", {
        "id": f"BPMNDiagram_{safe_id(process_id)}"
    })

    plane = ET.SubElement(diagram, f"{{{BPMNDI}}}BPMNPlane", {
        "id": f"BPMNPlane_{safe_id(process_id)}",
        "bpmnElement": process_id,
    })

    for nid, el in nodes.items():
        x, y, w, h = positions[nid]

        shape = ET.SubElement(plane, f"{{{BPMNDI}}}BPMNShape", {
            "id": f"{nid}_di",
            "bpmnElement": nid,
        })

        add_bounds(shape, x, y, w, h)

    for flow in flows:
        fid = flow.get("id")
        source = flow.get("sourceRef")
        target = flow.get("targetRef")

        if source not in positions or target not in positions:
            continue

        sx, sy, sw, sh = positions[source]
        tx, ty, tw, th = positions[target]

        source_x = sx + sw
        source_y = sy + sh / 2

        target_x = tx
        target_y = ty + th / 2

        edge = ET.SubElement(plane, f"{{{BPMNDI}}}BPMNEdge", {
            "id": f"{fid}_di",
            "bpmnElement": fid,
        })

        add_waypoint(edge, source_x, source_y)

        if abs(source_y - target_y) > 20:
            mid_x = (source_x + target_x) / 2
            add_waypoint(edge, mid_x, source_y)
            add_waypoint(edge, mid_x, target_y)

        add_waypoint(edge, target_x, target_y)

    backup = path.with_suffix(path.suffix + ".bak")

    if not backup.exists():
        shutil.copy2(path, backup)

    ET.indent(tree, space="  ", level=0)
    tree.write(path, encoding="UTF-8", xml_declaration=True)

    print(f"FIXED {path.name}: nodes={len(nodes)}, flows={len(flows)}")


def main():
    processes_dir = Path("src/main/resources/processes")

    if not processes_dir.exists():
        raise SystemExit("Run this script from the project root: D:/Repo/blps/blps1")

    for path in sorted(processes_dir.glob("*.bpmn")):
        fix_file(path)


if __name__ == "__main__":
    main()